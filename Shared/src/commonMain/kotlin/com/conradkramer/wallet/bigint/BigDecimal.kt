package com.conradkramer.wallet.bigint

import kotlin.math.pow

expect class BigDecimal {

    fun toDouble(): Double
    fun toBigInteger(): BigInteger

    fun div(other: BigDecimal, scale: Int): BigDecimal
    operator fun div(other: BigDecimal): BigDecimal
    operator fun times(valueOf: BigDecimal): BigDecimal

    companion object {
        fun valueOf(value: Double): BigDecimal
    }
}

val BigDecimal.Companion.CURRENCY_SCALE: Int
    get() = 3

internal fun BigDecimal.Companion.pow10(exponent: Int) = valueOf(10.0.pow(exponent.toDouble()))
