package com.conradkramer.wallet.bigint

actual data class BigInteger(val inner: java.math.BigInteger) {
    actual constructor(data: ByteArray) : this(java.math.BigInteger(1, data))

    actual fun toLong(): Long = inner.toLong()

    actual val data: ByteArray
        get() = if (inner == java.math.BigInteger.ZERO) ByteArray(0) else inner.toByteArray()

    actual companion object {
        actual fun valueOf(value: Long): BigInteger {
            return BigInteger(java.math.BigInteger.valueOf(value))
        }
    }
}
