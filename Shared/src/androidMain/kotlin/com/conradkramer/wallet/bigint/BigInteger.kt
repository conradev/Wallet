package com.conradkramer.wallet.bigint

import java.math.BigInteger

actual data class BigInteger(val inner: java.math.BigInteger) {
    actual constructor(data: ByteArray) : this(java.math.BigInteger(1, data))

    actual val data: ByteArray
        get() = if (inner == BigInteger.ZERO) ByteArray(0) else inner.toByteArray()
}
