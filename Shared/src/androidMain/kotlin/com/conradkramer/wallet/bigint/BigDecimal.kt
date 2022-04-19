package com.conradkramer.wallet.bigint

import java.math.MathContext
import java.math.RoundingMode

actual class BigDecimal(val inner: java.math.BigDecimal) {
    init {
        inner.setScale(1000000, RoundingMode.HALF_UP)
    }

    actual constructor(integer: BigInteger) : this(integer.inner.toBigDecimal())

    override fun toString(): String {
        return inner.toString()
    }

    actual fun toDouble(): Double {
        return inner.toDouble()
    }

    actual operator fun div(other: BigDecimal): BigDecimal {
        return BigDecimal(inner.divide(other.inner, MathContext.UNLIMITED))
    }

    actual companion object {
        actual fun valueOf(value: Double): BigDecimal {
            return BigDecimal(java.math.BigDecimal.valueOf(value))
        }
    }
}
