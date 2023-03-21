package com.conradkramer.wallet

import com.conradkramer.wallet.bigint.BigDecimal
import com.conradkramer.wallet.bigint.BigInteger
import com.conradkramer.wallet.bigint.pow10

data class Balance(
    val currency: Currency,
    val value: BigInteger
) {
    fun toDouble() = (value.toBigDecimal().div(BigDecimal.pow10(currency.decimals))).toDouble()

    operator fun plus(valueOf: Balance) = Balance(currency, value + valueOf.value)
        .also { if (currency.code != valueOf.currency.code) throw Exception("Cannot add balances from different currencies") }

    fun convert(currency: Currency, rate: Double): Balance {
        val newValue = when (rate) {
            0.0 -> BigInteger.valueOf(0)
            else -> {
                val converted = value.toBigDecimal() / BigDecimal.valueOf(rate)
                val scaled = converted * BigDecimal.pow10(currency.decimals) / BigDecimal.pow10(this.currency.decimals)
                scaled.toBigInteger()
            }
        }
        return Balance(currency, newValue)
    }
}
