package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.encoding.toByteArray
import io.ktor.utils.io.core.ByteOrder
import kotlinx.serialization.Serializable

@Serializable
internal sealed class Type {
    internal data class UInt(val bits: kotlin.Int) : Type() {
        override fun toString() = "uint$bits"
    }
    internal data class Int(val bits: kotlin.Int) : Type() {
        override fun toString() = "int$bits"
    }
    internal object Address : Type() {
        override fun toString() = "address"
    }
    internal object Bool : Type() {
        override fun toString() = "bool"
    }
    internal data class Fixed(val bits: kotlin.Int, val decimal: kotlin.Int) : Type() {
        override fun toString() = "fixed${bits}x$decimal"
    }
    internal data class UFixed(val bits: kotlin.Int, val decimal: kotlin.Int) : Type() {
        override fun toString() = "ufixed${bits}x$decimal"
    }
    internal data class Bytes(val size: kotlin.Int? = null) : Type() {
        override fun toString() = "bytes${size?.toString() ?: ""}"
    }
    internal object Function : Type() {
        override fun toString() = "function"
    }
    internal object String : Type() {
        override fun toString() = "string"
    }
    internal data class Array(val type: Type, val size: kotlin.Int? = null) {
        override fun toString() = "$type[${size?.toString() ?: ""}]"
    }
    internal data class Tuple(val types: List<Type>) {
        override fun toString() = types
            .map(Type::toString)
            .joinToString(",", "(", ")")
    }
}

internal fun Type.UInt.encode(value: UInt): ByteArray {
    val buffer = ByteArray(bits / 8)
    val encoded = value.toByteArray(ByteOrder.BIG_ENDIAN)
    encoded.copyInto(buffer, buffer.size - encoded.size)
    return encoded
}

internal fun Type.Bytes.encode(value: ByteArray): ByteArray {
    size?.let { size ->
        return value + ByteArray(32 - size)
    }

    TODO()
}
