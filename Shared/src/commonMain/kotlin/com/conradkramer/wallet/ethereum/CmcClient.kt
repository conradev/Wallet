@file:OptIn(ExperimentalSerializationApi::class)

package com.conradkramer.wallet.ethereum

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.serializersModuleOf
import mu.KLogger

/*
{
    "status": {
    "timestamp": "2022-05-29T04:50:10.539Z",
    "error_code": 0,
    "error_message": null,
    "elapsed": 794,
    "credit_count": 1,
    "notice": null
},
    "data": [
    {

    }*/

@Serializable
data class Status(
    val timestamp: Instant
)

@Serializable
data class ErrorStatus(
    val timestamp: Instant,
    @SerialName("error_code")
    val errorCode: Int,
    @SerialName("error_message")
    val errorMessage: String
) : Exception(errorMessage)

@Serializable
data class ErrorResponse(
    val status: ErrorStatus
)

@Serializable
data class DataResponse<Data>(
    val status: Status,
    val data: List<Data>
)

@Serializable
data class Platform(
    val id: Int,
    val slug: String,
    val name: String,
    val symbol: String,
    @SerialName("token_address")
    val tokenAddress: String
)

@Serializable
data class Currency(
    val id: Int,
    val name: String,
    val symbol: String,
    val slug: String,
    val rank: Int,
    @SerialName("is_active")
    val isActive: Int,
    val platform: Platform?
)

internal class CmcClient(
    var token: String,
    private val nativeLogger: KLogger
) {

    private val endpointUrl: Url = Url("https://pro-api.coinmarketcap.com/v1")

    private val client = HttpClient {
        install(Logging) {
            klogger(nativeLogger)
            level = LogLevel.INFO
        }
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun currencies(): List<Currency> {
        val request = client.get(endpointUrl) {
            url {
                appendPathSegments("cryptocurrency", "map")
            }
            header("X-CMC_PRO_API_KEY", token)
        }
        return request.body<DataResponse<Currency>>().data
    }

    companion object {
        private val json = Json {
            encodeDefaults = true
            explicitNulls = false
            ignoreUnknownKeys = true
            serializersModule = SerializersModule {
                include(serializersModuleOf(Currency.serializer()))
                include(serializersModuleOf(Status.serializer()))
            }
        }
    }
}
