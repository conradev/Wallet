package com.conradkramer.wallet.platform

expect class BigDecimal(integer: BigInteger) {

    fun toDouble(): Double

    operator fun div(other: BigDecimal): BigDecimal

    companion object {
        fun valueOf(value: Double): BigDecimal
    }
}
