package com.conradkramer.wallet.ethereum

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlin.random.Random

internal class RpcClient(val endpointUrl: Url) {

    val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend inline fun <reified Response> request(request: Request<Response>): Response {
        val jsonRpcRequest = request.jsonRpcRequest(Random.nextInt())
        val jsonRpcResponse = client
            .post(endpointUrl) {
                contentType(ContentType.Application.Json)
                setBody(jsonRpcRequest)
            }
            .body<JsonRpcResponse<Response>>()

        jsonRpcRequest.validate(jsonRpcResponse)
        jsonRpcResponse.error?.let { throw JsonRpcException(it) }

        return jsonRpcResponse.result
            ?: throw Exception("Result and error were both null")
    }
}
