@file:JvmName("BigIntegerAndroid")

package com.conradkramer.wallet.bigint

import com.conradkramer.wallet.encoding.RLP
import com.conradkramer.wallet.encoding.RLPRepresentable
import com.conradkramer.wallet.encoding.toByteArray
import com.conradkramer.wallet.encoding.toULong

actual data class BigInteger(val inner: java.math.BigInteger) : RLPRepresentable {
    actual constructor(data: ByteArray) : this(java.math.BigInteger(1, data))

    actual val data: ByteArray
        get() = if (inner == java.math.BigInteger.ZERO) ByteArray(0) else inner.toUnsignedByteArray()

    override val rlp: RLP.Item
        get() = RLP.Item.Data(data)

    actual fun toLong(): Long = inner.toLong()
    actual fun toULong(): ULong = data.toULong()

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BigInteger

        if (inner != other.inner) return false

        return true
    }

    actual override fun hashCode(): Int {
        return inner.hashCode()
    }

    actual companion object {
        actual fun valueOf(value: Long): BigInteger {
            return java.math.BigInteger.valueOf(value).wrap()
        }

        actual fun valueOf(value: ULong): BigInteger {
            return BigInteger(value.toByteArray())
        }
    }
}

internal fun java.math.BigInteger.toUnsignedByteArray(): ByteArray {
    val size = (bitLength() / 8) + if (bitLength().mod(8) != 0) 1 else 0
    val byteArray = toByteArray()
    return byteArray.sliceArray((byteArray.size - size) until byteArray.size)
}

internal fun java.math.BigInteger.wrap(): BigInteger {
    return BigInteger(this)
}
