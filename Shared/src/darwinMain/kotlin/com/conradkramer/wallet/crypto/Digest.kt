package com.conradkramer.wallet.crypto

import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.CCKeyDerivationPBKDF
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreCrypto.CC_SHA512_DIGEST_LENGTH
import platform.CoreCrypto.kCCHmacAlgSHA512
import platform.CoreCrypto.kCCPBKDF2
import platform.CoreCrypto.kCCPRFHmacAlgSHA512
import platform.CoreCrypto.kCCSuccess
import platform.posix.CHAR_BIT
import ripemd160.RIPEMD160_DIGEST_LENGTH
import ripemd160.ripemd160_digest
import xkcp.Keccak_HashFinal
import xkcp.Keccak_HashInitialize
import xkcp.Keccak_HashInstance
import xkcp.Keccak_HashUpdate

@OptIn(ExperimentalUnsignedTypes::class)
internal actual object SHA256Digest {
    actual fun digest(data: ByteArray) = UByteArray(CC_SHA256_DIGEST_LENGTH)
        .also { CC_SHA256(data.refTo(0), data.size.convert(), it.refTo(0)) }
        .toByteArray()
}

@OptIn(ExperimentalUnsignedTypes::class)
internal actual object SHA512Mac {
    actual fun authenticationCode(data: ByteArray, key: ByteArray) = UByteArray(CC_SHA512_DIGEST_LENGTH)
        .also {
            CCHmac(
                kCCHmacAlgSHA512,
                key.refTo(0),
                key.size.convert(),
                data.refTo(0),
                data.size.convert(),
                it.refTo(0)
            )
        }
        .toByteArray()
}

@OptIn(ExperimentalUnsignedTypes::class)
internal actual object PBKDF2SHA512Derivation {
    actual fun compute(
        salt: ByteArray,
        password: String,
        rounds: Int
    ): ByteArray = UByteArray(CC_SHA512_DIGEST_LENGTH)
        .also {
            CCKeyDerivationPBKDF(
                kCCPBKDF2,
                password,
                password.length.convert(),
                salt.asUByteArray().refTo(0),
                salt.size.convert(),
                kCCPRFHmacAlgSHA512,
                rounds.convert(),
                it.refTo(0),
                it.size.convert()
            )
                .also { if (it != kCCSuccess) throw Exception("PBKDF2 SHA-512 failed with status $it") }
        }
        .toByteArray()
}

@OptIn(ExperimentalUnsignedTypes::class)
internal actual object RIPEMD160Digest {
    actual fun digest(data: ByteArray) = UByteArray(RIPEMD160_DIGEST_LENGTH.convert())
        .also {
            ripemd160_digest(
                data.asUByteArray().refTo(0),
                data.size.convert(),
                it.refTo(0),
                it.size.convert()
            )
        }
        .toByteArray()
}

@OptIn(ExperimentalUnsignedTypes::class)
internal actual object Keccak256Digest {
    actual fun digest(data: ByteArray) = memScoped {
        val instance = alloc<Keccak_HashInstance>()
            .also { Keccak_HashInitialize(it.ptr, 1088, 512, 256, 1) }

        Keccak_HashUpdate(
            instance.ptr,
            data.asUByteArray().refTo(0),
            (data.size * CHAR_BIT).convert()
        )

        ByteArray(32)
            .also { Keccak_HashFinal(instance.ptr, it.asUByteArray().refTo(0)) }
    }
}
