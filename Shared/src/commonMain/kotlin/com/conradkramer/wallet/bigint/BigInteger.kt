package com.conradkramer.wallet.bigint

import com.conradkramer.wallet.encoding.RLPRepresentable

expect class BigInteger(data: ByteArray) : RLPRepresentable {
    val data: ByteArray

    fun toLong(): Long
    fun toULong(): ULong

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

    companion object {
        fun valueOf(value: Long): BigInteger
        fun valueOf(value: ULong): BigInteger
    }
}
