package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.ethereum.requests.JsonRpcResponse
import com.conradkramer.wallet.ethereum.requests.Request
import com.conradkramer.wallet.ethereum.requests.validate
import io.github.oshai.kotlinlogging.KLogger
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
import kotlin.math.absoluteValue
import kotlin.random.Random

internal class RpcClient(
    val endpointUrl: Url,
    private val nativeLogger: KLogger
) {
    val client = HttpClient {
        install(Logging) {
            klogger(nativeLogger)
            level = LogLevel.NONE
        }
        install(ContentNegotiation) {
            json(Request.json)
        }
    }

    suspend inline fun <reified Response> sendAndRetry(request: Request, maximumRetries: Int = 5): Response {
        var retries = maximumRetries
        while (true) {
            try {
                return send(request)
            } catch (e: Exception) {
                if (retries-- > 0) {
                    nativeLogger.error { "${request.method} request failed: $e, retrying" }
                } else {
                    throw e
                }
            }
        }
    }

    suspend inline fun <reified Response> send(request: Request): Response {
        /**
         * We need the absolute value because Cloudflare's API does not like negative IDs
         */
        val jsonRpcRequest = request.jsonRpcRequest(Random.nextInt().absoluteValue)

        val jsonRpcResponse = client
            .post(endpointUrl) {
                contentType(ContentType.Application.Json)
                setBody(jsonRpcRequest)
            }
            .body<JsonRpcResponse>()

        jsonRpcRequest.validate(jsonRpcResponse)
        jsonRpcResponse.error?.let { throw it }

        return jsonRpcResponse.result?.let { Request.decode(it) }
            ?: throw Exception("Result and error were both null")
    }
}

internal fun Logging.Config.klogger(klogger: KLogger) {
    logger = object : Logger { override fun log(message: String) = klogger.info { message } }
}
