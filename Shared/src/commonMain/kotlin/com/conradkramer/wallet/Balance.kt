package com.conradkramer.wallet

import com.conradkramer.wallet.bigint.BigDecimal
import com.conradkramer.wallet.bigint.BigInteger
import com.conradkramer.wallet.bigint.pow10

data class Balance(
    val currency: Currency,
    val value: BigInteger
) {
    fun toDouble() = (value.toBigDecimal().div(BigDecimal.pow10(currency.decimals))).toDouble()

    fun convert(currency: Currency, rate: Double): Balance {
        val newValue = if (rate == 0.0) {
            BigInteger.valueOf(0)
        } else {
            (
                value.toBigDecimal() / BigDecimal.valueOf(rate) * BigDecimal.pow10(currency.decimals) / BigDecimal.pow10(
                    this.currency.decimals
                )
                )
                .toBigInteger()
        }
        return Balance(currency, newValue)
    }
}
