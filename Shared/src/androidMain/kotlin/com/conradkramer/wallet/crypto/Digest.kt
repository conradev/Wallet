package com.conradkramer.wallet.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.MessageDigest
import java.security.Security
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

internal actual class SHA256Digest {
    actual fun digest(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(data)
    }
}

internal actual class SHA512Mac {
    actual fun authenticationCode(data: ByteArray, key: ByteArray): ByteArray {
        val mac: Mac = Mac.getInstance("HmacSHA512")
        mac.init(SecretKeySpec(key, "HmacSHA512"))
        return mac.doFinal(data)
    }
}

internal actual class PBKDF2SHA512Derivation actual constructor() {
    actual fun compute(
        salt: ByteArray,
        password: String,
        rounds: Int
    ): ByteArray {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        return factory.generateSecret(PBEKeySpec(password.toCharArray(), salt, rounds, 512)).encoded
    }
}

internal actual class RIPEMD160Digest {
    actual fun digest(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("RIPEMD160").digest(data)
    }

    companion object {
        init {
            installBouncyCastle()
        }
    }
}

internal actual class Keccak256Digest {
    actual fun digest(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("KECCAK-256").digest(data)
    }

    companion object {
        init {
            installBouncyCastle()
        }
    }
}

private fun installBouncyCastle() {
    Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
    Security.addProvider(BouncyCastleProvider())
}
