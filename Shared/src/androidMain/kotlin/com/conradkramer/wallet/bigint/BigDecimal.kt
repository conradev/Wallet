package com.conradkramer.wallet.bigint

import java.math.RoundingMode

actual class BigDecimal(val inner: java.math.BigDecimal) {
    override fun toString() = inner.toString()
    actual fun toDouble() = inner.toDouble()
    actual fun toBigInteger() = BigInteger(inner.toBigInteger())
    actual fun div(other: BigDecimal, scale: Int) = BigDecimal(inner.divide(other.inner, scale, RoundingMode.DOWN))
    actual operator fun div(other: BigDecimal) = div(other, CURRENCY_SCALE)
    actual operator fun times(valueOf: BigDecimal) = BigDecimal(inner.times(valueOf.inner))

    actual companion object {
        actual fun valueOf(value: Double) = BigDecimal(java.math.BigDecimal.valueOf(value))
    }
}
