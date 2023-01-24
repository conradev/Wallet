@file:OptIn(ExperimentalUnsignedTypes::class)

package com.conradkramer.wallet.crypto

import com.conradkramer.wallet.bigint.BigInteger
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readValue
import kotlinx.cinterop.refTo
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

internal actual class PrivateKey actual constructor(actual val encoded: ByteArray) {
    init {
        if (encoded.size != 32) throw Exception("Invalid private key")
    }

    actual val publicKey: PublicKey
        get() = PublicKey(
            memScoped {
                alloc<secp256k1_pubkey>()
                    .also {
                        secp256k1_ec_pubkey_create(context(), it.ptr, encoded.asUByteArray().refTo(0))
                            .also { if (it != 1) throw Exception("Failed to generate public key from private key") }
                    }
                    .readValue()
            }
        )

    actual operator fun plus(increment: PrivateKey) = PrivateKey(
        memScoped {
            encoded.copyOf()
                .also { sum ->
                    secp256k1_ec_seckey_tweak_add(
                        context(),
                        sum.asUByteArray().refTo(0),
                        increment.encoded.asUByteArray().refTo(0)
                    )
                        .also { if (it != 1) throw Exception("Failed add $this to $increment") }
                }
        }
    )

    actual fun sign(data: ByteArray) = memScoped {
        val hash = Keccak256Digest.digest(data)
        val entropy = SecureRandom.nextBytes(32)
        val context = context()
        val signature = alloc<secp256k1_ecdsa_recoverable_signature>()
            .also { signature ->
                secp256k1_ecdsa_sign_recoverable(
                    context,
                    signature.ptr,
                    hash.asUByteArray().refTo(0),
                    encoded.asUByteArray().refTo(0),
                    null,
                    entropy.asUByteArray().refTo(0)
                )
                    .also { if (it != 1) throw Exception("Failed to create signature") }
            }

        val recoveryId = alloc<IntVar>()
        val output = ByteArray(64)
            .also { output ->
                secp256k1_ecdsa_recoverable_signature_serialize_compact(
                    context,
                    output.asUByteArray().refTo(0),
                    recoveryId.ptr,
                    signature.ptr
                )
                    .also { if (it != 1) throw Exception("Failed to serialize signature") }
            }

        Signature(
            BigInteger(output.copyOfRange(0, 32)),
            BigInteger(output.copyOfRange(32, 64)),
            recoveryId.value.toByte()
        )
    }

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PrivateKey

        if (!encoded.contentEquals(other.encoded)) return false

        return true
    }

    actual override fun hashCode() = encoded.contentHashCode()
}

actual class PublicKey(private val key: CValue<secp256k1_pubkey>) {

    actual constructor (data: ByteArray) : this(parse(data))

    actual fun encoded(compressed: Boolean) = ByteArray(if (compressed) { 33 } else { 65 })
        .also { data ->
            val length = cValuesOf(data.size.toULong())
            val flags = if (compressed) SECP256K1_EC_COMPRESSED else SECP256K1_EC_UNCOMPRESSED
            memScoped {
                secp256k1_ec_pubkey_serialize(context(), data.asUByteArray().refTo(0), length, key.ptr, flags.convert())
                    .also { if (it != 1) throw Exception("Failed to serialize public key") }
            }
        }

    internal actual fun verify(data: ByteArray, signature: Signature) = memScoped {
        val context = context(SECP256K1_CONTEXT_VERIFY)
        val hash = Keccak256Digest.digest(data)
        secp256k1_ecdsa_verify(
            context,
            parseSignature(signature, context).ptr,
            hash.asUByteArray().refTo(0),
            key.ptr
        )
            .let { it == 1 }
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
        actual fun recover(data: ByteArray, signature: Signature) = memScoped {
            val context = context(SECP256K1_CONTEXT_VERIFY)
            val hash = Keccak256Digest.digest(data)

            PublicKey(
                alloc<secp256k1_pubkey>()
                    .also {
                        secp256k1_ecdsa_recover(
                            context,
                            it.ptr,
                            parseRecoverableSignature(signature, context).ptr,
                            hash.asUByteArray().refTo(0)
                        )
                            .also { if (it != 1) throw Exception("Invalid signature") }
                    }
                    .readValue()
            )
        }

        private fun parseSignature(signature: Signature, context: CPointer<secp256k1_context>): CValue<secp256k1_ecdsa_signature> = memScoped {
            alloc<secp256k1_ecdsa_signature>()
                .also {
                    secp256k1_ecdsa_recoverable_signature_convert(
                        context,
                        it.ptr,
                        parseRecoverableSignature(signature, context).ptr
                    )
                }
                .readValue()
        }

        private fun parseRecoverableSignature(signature: Signature, context: CPointer<secp256k1_context>): CValue<secp256k1_ecdsa_recoverable_signature> = memScoped {
            val buffer = signature.r.data + signature.s.data
            return alloc<secp256k1_ecdsa_recoverable_signature>()
                .also {
                    secp256k1_ecdsa_recoverable_signature_parse_compact(
                        context,
                        it.ptr,
                        buffer.asUByteArray().refTo(0),
                        signature.v.toInt()
                    )
                        .also { if (it != 1) throw Exception("Failed to parse signature") }
                }
                .readValue()
        }

        private fun parse(data: ByteArray): CValue<secp256k1_pubkey> = memScoped {
            alloc<secp256k1_pubkey>()
                .also { secp256k1_ec_pubkey_parse(context(), it.ptr, data.asUByteArray().refTo(0), data.size.convert()) }
                .readValue()
        }
    }
}

private fun MemScope.context(flags: Int = SECP256K1_CONTEXT_SIGN): CPointer<secp256k1_context> {
    val context = secp256k1_context_create(flags.convert())!!
    defer { secp256k1_context_destroy(context) }
    return context
}
