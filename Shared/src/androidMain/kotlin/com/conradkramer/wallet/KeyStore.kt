package com.conradkramer.wallet

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricPrompt
import com.conradkramer.wallet.platform.SHA256Digest
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.security.Key
import java.security.KeyPairGenerator
import java.security.KeyStore
import javax.crypto.Cipher

internal actual data class Authentication(val result: BiometricPrompt.AuthenticationResult)

internal actual class KeyStore(private val context: Context) {
    private companion object {
        fun cipher(encryptMode: Int, key: Key): Cipher {
            return Cipher.getInstance("RSA/ECB/PKCS1Padding")
                .apply { init(encryptMode, key) }
        }

        const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }

    private val isStrongBoxBacked by lazy {
        if (BuildConfig.DEBUG) {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
        } else {
            true
        }
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "seeds",
        Context.MODE_PRIVATE
    )

    private val androidKeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }

    private val biometricManager: BiometricManager by lazy {
        BiometricManager.from(context)
    }

    actual val canStore: Boolean
        get() = biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BIOMETRIC_SUCCESS

    actual val all: List<String>
        get() {
            val encryptedSeeds = sharedPreferences.all
            val labels = encryptedSeeds.keys
            val aliases = androidKeyStore.aliases().toList()

            if (aliases.size != labels.size) { throw Exception("Entries mismatched") }
            if (!aliases.containsAll(labels)) { throw Exception("Entries mismatched") }

            return aliases
        }

    actual fun add(data: ByteArray): String {
        val label = SHA256Digest().digest(data).encodeHex()

        val keyPairGenerator: KeyPairGenerator =
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)

        val purposes = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT

        keyPairGenerator.initialize(
            KeyGenParameterSpec.Builder(label, purposes)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setUserAuthenticationRequired(true)
                .setIsStrongBoxBacked(isStrongBoxBacked)
                .setKeySize(4096)
                .build()
        )

        val keyPair = keyPairGenerator.generateKeyPair()

        val cipher = cipher(Cipher.ENCRYPT_MODE, keyPair.public)

        val encryptedData = cipher.doFinal(data)

        sharedPreferences
            .edit()
            .putString(label, encryptedData.encodeBase64())
            .apply()

        return label
    }

    actual fun delete(id: String) {
        sharedPreferences.edit().remove(id).apply()
        androidKeyStore.deleteEntry(id)
    }

    fun getCryptoObject(id: String): BiometricPrompt.CryptoObject {
        val key = androidKeyStore.getEntry(id, null) as KeyStore.PrivateKeyEntry
        val decryptCipher = cipher(Cipher.DECRYPT_MODE, key.privateKey)
        return BiometricPrompt.CryptoObject(decryptCipher)
    }

    actual fun use(authentication: Authentication, id: String, use: (data: ByteArray) -> Unit) {
        val cipher = authentication.result.cryptoObject?.cipher
            ?: throw Exception("Cipher unavailable")

        val encryptedSeed = sharedPreferences.getString(id, null)?.decodeBase64Bytes()
            ?: throw Exception("Key not found for id: $id")

        val decryptedBytes = cipher.doFinal(encryptedSeed)
        use(decryptedBytes)
    }
}

internal actual fun keyStoreModule() = module {
    singleOf(::KeyStore)
}
