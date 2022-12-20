package com.conradkramer.wallet.bigint

expect class BigInteger(data: ByteArray) {
    val data: ByteArray

    fun toLong(): Long
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    companion object {
        fun valueOf(value: Long): BigInteger
    }
}
