package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.bigint.BigInteger
import com.conradkramer.wallet.encoding.toByteArray
import com.conradkramer.wallet.ethereum.types.Quantity
import io.ktor.utils.io.core.ByteOrder
import kotlinx.serialization.Serializable

@Serializable
internal sealed class Type {
    internal data class UInt(val bits: kotlin.Int = 256) : Type() {
        override fun toString() = "uint$bits"
    }
    internal data class Int(val bits: kotlin.Int = 256) : Type() {
        override fun toString() = "int$bits"
    }
    internal object Address : Type() {
        override fun toString() = "address"
    }
    internal object Bool : Type() {
        override fun toString() = "bool"
    }
    internal data class Fixed(val bits: kotlin.Int = 128, val decimal: kotlin.Int = 18) : Type() {
        override fun toString() = "fixed${bits}x$decimal"
    }
    internal data class UFixed(val bits: kotlin.Int = 128, val decimal: kotlin.Int = 18) : Type() {
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
        override fun toString() = types.joinToString(",", "(", ")", transform = Type::toString)
    }
}

internal fun Type.UInt.encode(value: BigInteger): ByteArray {
    val buffer = ByteArray(bits / 8)
    val encoded = value.data
    encoded.copyInto(buffer, buffer.size - encoded.size)
    return encoded
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

internal fun Type.Bool.decode(value: ByteArray): Boolean {
    if (value.size != 32) throw Exception("Invalid data size to turn into a boolean")
    return BigInteger(value).toLong() == 1L
}

internal fun Type.Bytes.decode(value: ByteArray): ByteArray {
    val cursor = Cursor(value)

    size?.let { return cursor.read(it) }

    val sizeSize = Quantity(cursor.read(32)).toInt()
    val size = Quantity(cursor.read(sizeSize)).toInt()
    return cursor.read(size)
}

internal fun Type.UInt.decode(value: ByteArray): BigInteger {
    return BigInteger(value)
}

internal fun Type.UInt.decodeLong(value: ByteArray) = decode(value).toLong()
internal fun Type.UInt.decodeInt(value: ByteArray) = decodeLong(value).toInt()
internal fun Type.String.decode(value: ByteArray) = Type.Bytes().decode(value).decodeToString()

private class Cursor(val data: ByteArray) {
    private var offset = 0

    fun read(size: Int): ByteArray {
        val result = data.copyOfRange(offset, offset + size)
        offset += size
        return result
    }
}
