package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.Currency
import com.conradkramer.wallet.bigint.BigDecimal
import com.conradkramer.wallet.ethereum.Quantity
import kotlin.math.pow

data class BalanceViewModel internal constructor(
    val currency: Currency,
    val balance: Quantity
) {
    private val balanceDouble: Double
        get() = (BigDecimal(balance.value) / BigDecimal.valueOf(10.0.pow(currency.decimals))).toDouble()

    private val balanceString: String
        get() = "$balanceDouble"

    val currencyName: String
        get() = currency.displayName

    val formattedConvertedBalance: String
        get() = "$0.00"

    val formattedBalance: String
        get() = "$balanceString ${currency.symbol}"
}
