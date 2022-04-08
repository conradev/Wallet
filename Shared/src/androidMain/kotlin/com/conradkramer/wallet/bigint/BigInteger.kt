package com.conradkramer.wallet.bigint

actual data class BigInteger(val inner: java.math.BigInteger) {
    actual constructor(data: ByteArray) : this(java.math.BigInteger(1, data))

    actual val data: ByteArray
        get() = inner.toByteArray()
}
