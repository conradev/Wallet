package com.conradkramer.wallet.platform

actual data class BigInteger(val inner: java.math.BigInteger) {
    actual constructor(bytes: ByteArray) : this(java.math.BigInteger(bytes))
}
