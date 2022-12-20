package com.conradkramer.wallet.crypto

import com.conradkramer.wallet.bigint.BigInteger
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.Pinned
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readValue
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import secp256k1.SECP256K1_CONTEXT_SIGN
import secp256k1.SECP256K1_CONTEXT_VERIFY
import secp256k1.SECP256K1_EC_COMPRESSED
import secp256k1.SECP256K1_EC_UNCOMPRESSED
import secp256k1.secp256k1_context
import secp256k1.secp256k1_context_create
import secp256k1.secp256k1_context_destroy
import secp256k1.secp256k1_ec_pubkey_create
import secp256k1.secp256k1_ec_pubkey_parse
import secp256k1.secp256k1_ec_pubkey_serialize
import secp256k1.secp256k1_ec_seckey_tweak_add
import secp256k1.secp256k1_ecdsa_recover
import secp256k1.secp256k1_ecdsa_recoverable_signature
import secp256k1.secp256k1_ecdsa_recoverable_signature_convert
import secp256k1.secp256k1_ecdsa_recoverable_signature_parse_compact
import secp256k1.secp256k1_ecdsa_recoverable_signature_serialize_compact
import secp256k1.secp256k1_ecdsa_sign_recoverable
import secp256k1.secp256k1_ecdsa_signature
import secp256k1.secp256k1_ecdsa_verify
import secp256k1.secp256k1_pubkey

@OptIn(ExperimentalUnsignedTypes::class)
internal actual class PrivateKey actual constructor(private val data: ByteArray) {
    init {
        if (data.size != 32) {
            throw Exception("Invalid private key")
        }
    }

    actual val publicKey: PublicKey
        get() = memScoped {
            val key = alloc<secp256k1_pubkey>()
            data.asUByteArray()
                .usePinned {
                    secp256k1_ec_pubkey_create(context(), key.ptr, it.addressOf(0))
                }
                .also { if (it != 1) throw Exception("Failed to generate public key from private key") }
            PublicKey(key.readValue())
        }

    actual val encoded: ByteArray
        get() = data

    actual operator fun plus(increment: PrivateKey): PrivateKey {
        val sum = data.copyOf()
        memScoped {
            (sum to increment.data)
                .let { (it.first.asUByteArray() to it.second.asUByteArray()) }
                .usePinned { pinnedSum, pinnedIncrement ->
                    secp256k1_ec_seckey_tweak_add(
                        context(),
                        pinnedSum.addressOf(0),
                        pinnedIncrement.addressOf(0)
                    )
                }
                .also { if (it != 1) throw Exception("Failed add $this to $increment") }
        }
        return PrivateKey(sum)
    }

    actual fun sign(data: ByteArray): Signature {
        val key = this.data
        val hash = Keccak256Digest.digest(data)
        val entropy = SecureRandom.nextBytes(32)
        return memScoped {
            val context = context()
            val signature = alloc<secp256k1_ecdsa_recoverable_signature>()
            hash
                .asUByteArray()
                .usePinned { pinnedHash ->
                    (entropy.asUByteArray() to key.asUByteArray()).usePinned { pinnedEntropy, pinnedKey ->
                        secp256k1_ecdsa_sign_recoverable(
                            context,
                            signature.ptr,
                            pinnedHash.addressOf(0),
                            pinnedKey.addressOf(0),
                            null,
                            pinnedEntropy.addressOf(0)
                        )
                    }
                }
                .also { if (it != 1) throw Exception("Failed to create signature") }

            val output = ByteArray(64)
            val recoveryId = alloc<IntVar>()
            output.asUByteArray()
                .usePinned { pinnedOutput ->
                    secp256k1_ecdsa_recoverable_signature_serialize_compact(
                        context,
                        pinnedOutput.addressOf(0),
                        recoveryId.ptr,
                        signature.ptr
                    )
                }
                .also { if (it != 1) throw Exception("Failed to serialize signature") }

            Signature(
                BigInteger(output.copyOfRange(0, 32)),
                BigInteger(output.copyOfRange(32, 64)),
                recoveryId.value.toByte()
            )
        }
    }

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PrivateKey

        if (!data.contentEquals(other.data)) return false

        return true
    }

    actual override fun hashCode(): Int {
        return data.contentHashCode()
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
actual class PublicKey(private val key: CValue<secp256k1_pubkey>) {

    actual constructor (data: ByteArray) : this(parseKey(data))

    actual fun encoded(compressed: Boolean): ByteArray {
        val data = ByteArray(if (compressed) { 33 } else { 65 })
        val length = cValuesOf(data.size.toULong())
        val flags = if (compressed) { SECP256K1_EC_COMPRESSED } else { SECP256K1_EC_UNCOMPRESSED }
        memScoped {
            data.asUByteArray()
                .usePinned {
                    secp256k1_ec_pubkey_serialize(context(), it.addressOf(0), length, key.ptr, flags.convert())
                }
                .also { if (it != 1) throw Exception("Failed to serialize public key") }
        }
        return data
    }

    internal actual fun verify(data: ByteArray, signature: Signature): Boolean {
        return memScoped {
            val context = context(SECP256K1_CONTEXT_VERIFY)
            val parsed = parseSignature(signature)
            Keccak256Digest.digest(data)
                .asUByteArray()
                .usePinned { hash ->
                    secp256k1_ecdsa_verify(
                        context,
                        parsed.ptr,
                        hash.addressOf(0),
                        key.ptr
                    )
                }
                .let { it == 1 }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PublicKey

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    internal actual companion object {
        actual fun recover(data: ByteArray, signature: Signature): PublicKey {
            return memScoped {
                val context = context(SECP256K1_CONTEXT_VERIFY)
                val parsed = parseRecoverableSignature(signature)
                val recoveredKey = alloc<secp256k1_pubkey>()
                Keccak256Digest.digest(data)
                    .asUByteArray()
                    .usePinned { hash ->
                        secp256k1_ecdsa_recover(
                            context,
                            recoveredKey.ptr,
                            parsed.ptr,
                            hash.addressOf(0)
                        )
                    }
                    .also { if (it != 1) throw Exception("Invalid signature") }
                val publicKey = PublicKey(recoveredKey.readValue())
                if (!publicKey.verify(data, signature)) throw Exception("Invalid signature")
                publicKey
            }
        }

        private fun parseSignature(signature: Signature): CValue<secp256k1_ecdsa_signature> {
            val recoverableSignature = parseRecoverableSignature(signature)
            return memScoped {
                val context = context(SECP256K1_CONTEXT_VERIFY)
                val converted = alloc<secp256k1_ecdsa_signature>()
                secp256k1_ecdsa_recoverable_signature_convert(context, converted.ptr, recoverableSignature.ptr)
                    .also { if (it != 1) throw Exception("Failed to convert signature") }

                converted.readValue()
            }
        }

        private fun parseRecoverableSignature(signature: Signature): CValue<secp256k1_ecdsa_recoverable_signature> {
            return memScoped {
                val context = context(SECP256K1_CONTEXT_VERIFY)
                val buffer = signature.r.data + signature.s.data
                val parsed = alloc<secp256k1_ecdsa_recoverable_signature>()
                buffer
                    .asUByteArray()
                    .usePinned { pinnedBuffer ->
                        secp256k1_ecdsa_recoverable_signature_parse_compact(
                            context,
                            parsed.ptr,
                            pinnedBuffer.addressOf(0),
                            signature.v.toInt()
                        )
                    }
                    .also { if (it != 1) throw Exception("Failed to parse signature") }

                parsed.readValue()
            }
        }

        private fun parseKey(data: ByteArray): CValue<secp256k1_pubkey> {
            return memScoped {
                val key = alloc<secp256k1_pubkey>()
                data.asUByteArray()
                    .usePinned {
                        secp256k1_ec_pubkey_parse(context(), key.ptr, it.addressOf(0), data.size.convert())
                    }
                    .also { if (it != 1) throw Exception("Failed to parse public key") }
                key.readValue()
            }
        }
    }
}

private fun MemScope.context(flags: Int = SECP256K1_CONTEXT_SIGN): CPointer<secp256k1_context> {
    val context = secp256k1_context_create(flags.convert())!!
    defer { secp256k1_context_destroy(context) }
    return context
}

private fun <T : Any, U : Any, R> Pair<T, U>.usePinned(block: (Pinned<T>, Pinned<U>) -> R): R {
    return first.usePinned { first ->
        second.usePinned { second ->
            block(first, second)
        }
    }
}
