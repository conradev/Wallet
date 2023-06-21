package com.conradkramer.wallet.viewmodel

import app.cash.sqldelight.coroutines.asFlow
import com.conradkramer.wallet.Balance
import com.conradkramer.wallet.Currency
import com.conradkramer.wallet.Currency.Code.Companion.ETH
import com.conradkramer.wallet.Locale
import com.conradkramer.wallet.NumberFormatter
import com.conradkramer.wallet.bigint.BigDecimal
import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.sql.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.math.pow

data class Asset(
    val balance: Balance,
    val fiat: Balance,
) {
    constructor(balance: Balance, fiat: Currency, rate: Double) : this(balance, balance.convert(fiat, rate))

    val balanceString: String
        get() = NumberFormatter.cryptocurrency(balance.currency).string(balance.toDouble())

    val fiatBalanceString: String
        get() = NumberFormatter.fiat(fiat.currency).string(fiat.toDouble())
}

class BalanceAccessor(
    val database: Database,
) {
    fun holdings(scope: CoroutineScope, chain: Chain, address: Address, fiat: Currency.Code): StateFlow<List<Asset>> {
        val ethBalanceFlow = database.ethereumQueries.balanceForAddress(fiat, chain, address).asFlow()
        val tokenBalanceFlow = database.ethereumQueries.tokenBalancesForAddress(fiat, chain, address).asFlow()

        return ethBalanceFlow
            .combine(tokenBalanceFlow) { _, _ -> holdings(chain, database, address, fiat) }
            .stateIn(scope, SharingStarted.WhileSubscribed(), holdings(chain, database, address, fiat))
    }

    companion object {
        fun holdings(chain: Chain, database: Database, address: Address, fiat: Currency.Code?): List<Asset> {
            val fiatCurrency = fiat?.let { Locale.current.currency(it) } ?: return emptyList()

            val ethBalance = listOfNotNull(
                database.ethereumQueries.balanceForAddress(fiat, chain, address).executeAsOneOrNull()?.let { result ->
                    val rate = result.rate ?: return@let null
                    Asset(Balance(Currency.ETH, result.balance.value), fiatCurrency, rate)
                },
            )

            val tokenBalances = database.ethereumQueries.tokenBalancesForAddress(fiat, chain, address).executeAsList()
                .mapNotNull { result ->
                    val rate = result.rate ?: return@mapNotNull null
                    if (result.symbol == null || result.name == null || result.decimals == null) return@mapNotNull null
                    val currency = Currency(result.symbol, result.name, null, result.decimals.toInt())
                    val balance = Balance(currency, result.balance.value)
                    Asset(balance, balance.convert(fiatCurrency, rate))
                }

            return (ethBalance + tokenBalances)
                .filter { it.balance.value.toULong() > 0UL }
        }
    }
}

private fun BigDecimal.Companion.pow10(exponent: Int) = valueOf(10.0.pow(exponent.toDouble()))
