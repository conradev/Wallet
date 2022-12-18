package com.conradkramer.wallet.bigint

expect class BigInteger(data: ByteArray) {
    val data: ByteArray

    fun toLong(): Long

    companion object {
        fun valueOf(value: Long): BigInteger
    }
}
