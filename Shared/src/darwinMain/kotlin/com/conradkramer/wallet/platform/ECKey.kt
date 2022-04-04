package com.conradkramer.wallet.platform

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readValue
import kotlinx.cinterop.usePinned
import secp256k1.SECP256K1_CONTEXT_SIGN
import secp256k1.SECP256K1_EC_COMPRESSED
import secp256k1.SECP256K1_EC_UNCOMPRESSED
import secp256k1.secp256k1_context
import secp256k1.secp256k1_context_create
import secp256k1.secp256k1_context_destroy
import secp256k1.secp256k1_ec_pubkey_create
import secp256k1.secp256k1_ec_pubkey_parse
import secp256k1.secp256k1_ec_pubkey_serialize
import secp256k1.secp256k1_ec_seckey_tweak_add
import secp256k1.secp256k1_pubkey

abstract class ECKey {
    companion object {
        fun <R> useContext(flags: Int = SECP256K1_CONTEXT_SIGN, content: (CPointer<secp256k1_context>) -> R): R {
            val context = secp256k1_context_create(flags.convert())!!
            val result = content(context)
            secp256k1_context_destroy(context)
            return result
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
internal actual class PrivateKey actual constructor(private val data: ByteArray) : ECKey() {
    init {
        if (data.size != 32) {
            throw Exception("Invalid private key")
        }
    }

    actual val publicKey: PublicKey
        get() {
            memScoped {
                val key = alloc<secp256k1_pubkey>()
                val result = useContext { context ->
                    data.asUByteArray().usePinned {
                        secp256k1_ec_pubkey_create(context, key.ptr, it.addressOf(0))
                    }
                }
                if (result != 1) {
                    throw Exception("Unable to generate public key from private key")
                }
                return PublicKey(key.readValue())
            }
        }

    actual val encoded: ByteArray
        get() = data

    actual operator fun plus(increment: PrivateKey): PrivateKey {
        val sum = data.copyOf()
        val result = memScoped {
            useContext { context ->
                sum.asUByteArray().usePinned { sumData ->
                    increment.data.asUByteArray().usePinned { incrementData ->
                        secp256k1_ec_seckey_tweak_add(context, sumData.addressOf(0), incrementData.addressOf(0))
                    }
                }
            }
        }
        if (result != 1) {
            throw Exception("Unable add $this to $increment")
        }
        return PrivateKey(sum)
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
actual class PublicKey(private val key: CValue<secp256k1_pubkey>) : ECKey() {

    actual constructor (data: ByteArray) : this(parse(data))

    actual fun encoded(compressed: Boolean): ByteArray {
        val data = ByteArray(if (compressed) { 33 } else { 65 })
        val length = cValuesOf(data.size.toULong())
        val flags = if (compressed) { SECP256K1_EC_COMPRESSED } else { SECP256K1_EC_UNCOMPRESSED }
        val status = memScoped {
            useContext { context ->
                data.asUByteArray().usePinned {
                    secp256k1_ec_pubkey_serialize(context, it.addressOf(0), length, key.ptr, flags.convert())
                }
            }
        }
        if (status != 1) {
            throw Exception("Failed to serialize public key")
        }
        return data
    }

    private companion object {
        fun parse(data: ByteArray): CValue<secp256k1_pubkey> {
            memScoped {
                val key = alloc<secp256k1_pubkey>()
                val status = useContext { context ->
                    data.asUByteArray().usePinned {
                        secp256k1_ec_pubkey_parse(context, key.ptr, it.addressOf(0), data.size.convert())
                    }
                }
                if (status != 1) {
                    throw Exception("Failed to parse public key")
                }
                return key.readValue()
            }
        }
    }
}
