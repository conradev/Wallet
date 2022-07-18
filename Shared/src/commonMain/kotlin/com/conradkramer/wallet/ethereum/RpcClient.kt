package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.ethereum.requests.JsonRpcException
import com.conradkramer.wallet.ethereum.requests.JsonRpcResponse
import com.conradkramer.wallet.ethereum.requests.Request
import com.conradkramer.wallet.ethereum.requests.validate
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import mu.KLogger
import kotlin.math.absoluteValue
import kotlin.random.Random

internal class RpcClient(val endpointUrl: Url, private val nativeLogger: KLogger) {

    val client = HttpClient {
        install(Logging) {
            klogger(nativeLogger)
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            json(Request.json)
        }
    }

    suspend inline fun <reified Response> send(request: Request): Response {
        /**
         * We need the absolute value because Cloudflare's API does not like negative IDs
         */
        val jsonRpcRequest = request.jsonRpcRequest(Random.nextInt().absoluteValue)
        nativeLogger.info { "Sending request $jsonRpcRequest" }

        val jsonRpcResponse = client
            .post(endpointUrl) {
                contentType(ContentType.Application.Json)
                setBody(jsonRpcRequest)
            }
            .body<JsonRpcResponse<Response>>()
        nativeLogger.info { "Received response $jsonRpcResponse" }

        jsonRpcResponse.error?.let { throw JsonRpcException(it) }
        jsonRpcRequest.validate(jsonRpcResponse)

        return jsonRpcResponse.result
            ?: throw Exception("Result and error were both null")
    }
}

fun Logging.Config.klogger(klogger: KLogger) {
    logger = object : Logger {
        override fun log(message: String) {
            klogger.info { message }
        }
    }
}
