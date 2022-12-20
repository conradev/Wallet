package com.conradkramer.wallet.bigint


actual data class BigInteger(val inner: java.math.BigInteger) : RLPRepresentable {
    actual constructor(data: ByteArray) : this(java.math.BigInteger(1, data))

    actual val data: ByteArray
        get() = if (inner == java.math.BigInteger.ZERO) ByteArray(0) else inner.toByteArray()

    actual fun toLong(): Long = inner.toLong()

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
    }
}

internal fun java.math.BigInteger.wrap(): BigInteger {
    return BigInteger(this)
}
