package com.conradkramer.wallet.indexing

import com.conradkramer.wallet.Currency
import com.conradkramer.wallet.clients.CoinbaseClient
import com.conradkramer.wallet.data.Cb_crypto_currency
import com.conradkramer.wallet.data.Cb_exchange_rate
import com.conradkramer.wallet.sql.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import mu.KLogger

internal class CoinbaseIndexer(
    private val scope: CoroutineScope,
    private val currencyCode: Currency.Code,
    private val client: CoinbaseClient,
    private val database: Database,
    private val logger: KLogger
) {
    init { refresh() }

    fun refresh() {
        scope.launch {
            try { index() } catch (e: Exception) {
                logger.error { "Failed to update Coinbase currencies and exchange rates: $e" }
            }
        }
    }

    suspend fun index() {
        logger.info { "Indexing Coinbase currencies and exchange rates" }

        val currencies = client.currencies()
        database.transaction {
            val now = Clock.System.now()
            for (currency in currencies) {
                database.coinbaseQueries.upsertCryptoCurrency(
                    Cb_crypto_currency(
                        currency.code,
                        currency.name,
                        currency.color,
                        currency.sortIndex,
                        currency.exponent,
                        currency.assetId,
                        now
                    )
                )
            }
        }

        logger.info { "Indexed ${currencies.size} currencies from Coinbase" }

        val exchangeRates = client.exchangeRates(currencyCode)
        database.transaction {
            val now = Clock.System.now()
            for (rate in exchangeRates) {
                database.coinbaseQueries.upsertExchangeRate(
                    Cb_exchange_rate(
                        currencyCode,
                        rate.key,
                        rate.value,
                        now
                    )
                )
            }
        }

        logger.info { "Indexed ${exchangeRates.size} exchange rates for $currencyCode from Coinbase" }
    }
}
