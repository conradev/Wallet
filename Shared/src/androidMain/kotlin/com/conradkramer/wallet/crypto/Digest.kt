package com.conradkramer.wallet.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.MessageDigest
import java.security.Security
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

internal actual object SHA256Digest {
    actual fun digest(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(data)
    }
}

internal actual object SHA512Mac {
    actual fun authenticationCode(data: ByteArray, key: ByteArray): ByteArray {
        val mac: Mac = Mac.getInstance("HmacSHA512")
        mac.init(SecretKeySpec(key, "HmacSHA512"))
        return mac.doFinal(data)
    }
}

internal actual object PBKDF2SHA512Derivation {
    actual fun compute(
        salt: ByteArray,
        password: String,
        rounds: Int
    ): ByteArray {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        return factory.generateSecret(PBEKeySpec(password.toCharArray(), salt, rounds, 512)).encoded
    }
}

internal actual object RIPEMD160Digest {
    actual fun digest(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("RIPEMD160").digest(data)
    }

    val bouncyCastle = Keccak256Digest.bouncyCastle
}

internal actual object Keccak256Digest {
    actual fun digest(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("KECCAK-256").digest(data)
    }

    val bouncyCastle = run {
        val provider = BouncyCastleProvider()
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.addProvider(provider)
        provider
    }
}
