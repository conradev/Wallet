package com.conradkramer.wallet.clients

import com.conradkramer.wallet.Currency
import com.conradkramer.wallet.ethereum.klogger
import io.github.oshai.kotlinlogging.KLogger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import io.ktor.http.parametersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class CoinbaseClient(
    private val nativeLogger: KLogger
) {
    @Serializable
    internal data class Cryptocurrency(
        val code: Currency.Code,
        val name: String,
        val color: String,
        @SerialName("sort_index")
        val sortIndex: Long,
        val exponent: Long,
        @SerialName("asset_id")
        val assetId: String
    )

    @Serializable
    private data class ExchangeRates(
        val currency: String,
        val rates: Map<Currency.Code, Double>
    )

    @Serializable
    private data class DataResponse<Data>(val data: Data)

    private val client = HttpClient {
        install(Logging) {
            klogger(nativeLogger)
            level = LogLevel.NONE
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    private val endpointUrl = Url("https://api.coinbase.com/v2")

    suspend fun currencies(): List<Cryptocurrency> = client.get(endpointUrl) {
        url { appendPathSegments("currencies", "crypto") }
    }
        .body<DataResponse<List<Cryptocurrency>>>()
        .data

    suspend fun exchangeRates(currency: Currency.Code): Map<Currency.Code, Double> = client.get(endpointUrl) {
        url {
            appendPathSegments("exchange-rates")
            parametersOf("currency", currency.code)
        }
    }
        .body<DataResponse<ExchangeRates>>()
        .data
        .rates
}
