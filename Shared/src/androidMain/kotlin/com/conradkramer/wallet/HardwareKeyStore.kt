package com.conradkramer.wallet

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.KeyPairGenerator
import java.security.KeyStore.PrivateKeyEntry
import javax.crypto.Cipher
import kotlin.coroutines.resume

actual typealias BiometricPromptHost = FragmentActivity

internal actual class HardwareKeyStore(context: Context) : KeyStore<AuthenticationContext> {

    private val keyStore = java.security.KeyStore.getInstance(ANDROID_KEYSTORE)
        .apply { load(null) }

    private val biometricManager = BiometricManager.from(context)
    private val hasStrongBox = context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)

    override val canStore: Boolean
        get() = biometricManager.canAuthenticate(authenticators) == BIOMETRIC_SUCCESS

    override val all: Set<String>
        get() = keyStore.aliases().toList().toSet()

    override fun generate(id: String) {
        val purposes = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
        val builder = KeyGenParameterSpec.Builder(id, purposes)
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .setUserAuthenticationRequired(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setDevicePropertiesAttestationIncluded(false)
        }
        keyPairGenerator.initialize(builder.build())
        keyPairGenerator.generateKeyPair()
    }

    override fun encrypt(id: String, data: ByteArray): ByteArray {
        return context(id, true).cipher.doFinal(data)
    }

    override fun delete(id: String) {
        keyStore.deleteEntry(id)
    }

    override fun reset() {
        all.forEach { delete(it) }
    }

    override fun context(id: String): AuthenticationContext {
        return context(id, false)
    }

    private fun context(id: String, encrypt: Boolean): AuthenticationContext {
        val entry = keyStore.getEntry(id, null) as PrivateKeyEntry
        return AuthenticationContext(id).apply { configure(encrypt, entry) }
    }

    override suspend fun prompt(
        context: AuthenticationContext,
        host: BiometricPromptHost?,
        info: BiometricPromptInfo,
    ): Boolean {
        val activity = host ?: return false
        return suspendCancellableCoroutine { continuation ->
            val prompt = BiometricPrompt(
                activity,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        continuation.resume(true)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        continuation.resume(false)
                    }

                    override fun onAuthenticationFailed() {
                        continuation.resume(false)
                    }
                },
            )
            prompt.authenticate(info.info, context.cryptoObject)
            continuation.invokeOnCancellation {
                prompt.cancelAuthentication()
            }
        }
    }

    override fun <R> decrypt(context: AuthenticationContext, data: ByteArray, handler: (data: ByteArray) -> R): R {
        return handler(context.cipher.doFinal(data))
    }

    companion object {
        const val authenticators = BIOMETRIC_WEAK
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }
}

actual class AuthenticationContext(
    actual val id: String,
    val cryptoObject: BiometricPrompt.CryptoObject,
) {
    actual constructor(id: String) : this(
        id,
        BiometricPrompt.CryptoObject(
            Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding"),
        ),
    )

    val cipher: Cipher
        get() = cryptoObject.cipher!!

    fun configure(encrypt: Boolean, entry: PrivateKeyEntry) {
        if (encrypt) {
            cipher.init(Cipher.ENCRYPT_MODE, entry.certificate.publicKey)
        } else {
            cipher.init(Cipher.DECRYPT_MODE, entry.privateKey)
        }
    }
}

private val BiometricPromptInfo.info: BiometricPrompt.PromptInfo
    get() = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setDescription(reason)
        .setAllowedAuthenticators(HardwareKeyStore.authenticators)
        .setNegativeButtonText(cancelTitle)
        .build()
