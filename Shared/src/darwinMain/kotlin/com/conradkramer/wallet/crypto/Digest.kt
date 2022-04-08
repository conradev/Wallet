package com.conradkramer.wallet.crypto

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
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
    actual fun digest(data: ByteArray): ByteArray {
        val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
        data.usePinned { inputPinned ->
            digest.usePinned { digestPinned ->
                CC_SHA256(
                    inputPinned.addressOf(0),
                    data.size.convert(),
                    digestPinned.addressOf(0)
                )
            }
        }
        return digest.toByteArray()
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
internal actual object SHA512Mac {
    actual fun authenticationCode(data: ByteArray, key: ByteArray): ByteArray {
        val digest = UByteArray(CC_SHA512_DIGEST_LENGTH)
        digest.usePinned { pinnedDigest ->
            data.usePinned { pinnedData ->
                key.usePinned { pinnedKey ->
                    CCHmac(
                        kCCHmacAlgSHA512,
                        pinnedKey.addressOf(0),
                        key.size.convert(),
                        pinnedData.addressOf(0),
                        data.size.convert(),
                        pinnedDigest.addressOf(0)
                    )
                }
            }
        }
        return digest.toByteArray()
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
internal actual object PBKDF2SHA512Derivation {
    actual fun compute(
        salt: ByteArray,
        password: String,
        rounds: Int
    ): ByteArray {
        val key = UByteArray(CC_SHA512_DIGEST_LENGTH)
        val status = key.usePinned { keyPinned ->
            salt.toUByteArray().usePinned { saltPinned ->
                CCKeyDerivationPBKDF(
                    kCCPBKDF2,
                    password,
                    password.length.convert(),
                    saltPinned.addressOf(0),
                    salt.size.convert(),
                    kCCPRFHmacAlgSHA512,
                    rounds.convert(),
                    keyPinned.addressOf(0),
                    key.size.convert()
                )
            }
        }
        when (status) {
            kCCSuccess -> {}
            else -> throw Exception("Failed to calculate PBKDF2: $status")
        }

        return key.toByteArray()
    }

    private const val CC_SHA512_DIGEST_LENGTH = 64
}

@OptIn(ExperimentalUnsignedTypes::class)
internal actual object RIPEMD160Digest {
    actual fun digest(data: ByteArray): ByteArray {
        val digest = UByteArray(RIPEMD160_DIGEST_LENGTH.convert())
        digest.usePinned { pinnedDigest ->
            data.asUByteArray().usePinned { pinnedData ->
                ripemd160_digest(
                    pinnedData.addressOf(0),
                    data.size.convert(),
                    pinnedDigest.addressOf(0),
                    digest.size.convert()
                )
            }
        }
        return digest.toByteArray()
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
internal actual object Keccak256Digest {
    actual fun digest(data: ByteArray): ByteArray {
        memScoped {
            val instance = alloc<Keccak_HashInstance>()
            Keccak_HashInitialize(instance.ptr, 1088, 512, 256, 1)
            data.asUByteArray().usePinned {
                Keccak_HashUpdate(instance.ptr, it.addressOf(0), (data.size * CHAR_BIT).convert())
            }
            val digest = ByteArray(32)
            digest.asUByteArray().usePinned {
                Keccak_HashFinal(instance.ptr, it.addressOf(0))
            }
            return digest
        }
    }
}
