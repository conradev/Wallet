package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.ethereum.requests.Call
import com.conradkramer.wallet.ethereum.requests.GetBalance
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer

internal abstract class Request<Response> {
    abstract val method: String
    abstract val params: List<JsonElement>

    fun jsonRpcRequest(id: Int): JsonRpcRequest {
        return JsonRpcRequest(method = method, params = params, id = id)
    }

    companion object {
        inline fun <reified T> encode(value: T): JsonElement {
            return Json.encodeToJsonElement(serializer(), value)
        }

        inline fun <reified T> decode(
            params: List<JsonElement>,
            index: Int,
            serializer: KSerializer<T> = serializer()
        ): T {
            return Json.decodeFromJsonElement(serializer, params[index])
        }

        inline fun <reified T> decode(
            params: List<JsonElement>,
            index: Int,
            default: T,
            serializer: KSerializer<T> = serializer()
        ): T {
            return if (index < params.size) {
                Json.decodeFromJsonElement(serializer, params[index])
            } else {
                default
            }
        }

        private val mapping: Map<String, (List<JsonElement>) -> Request<*>> = mapOf(
            ("eth_getBalance" to ::GetBalance),
            ("eth_call" to ::Call)
        )

        fun fromRequest(request: JsonRpcRequest): Request<*> {
            val constructor = mapping[request.method]
                ?: throw Exception("Unknown request method")

            return constructor(request.params)
        }
    }
}

@Serializable
internal data class JsonRpcRequest(
    var jsonrpc: String = "2.0",
    var method: String,
    var id: Int,
    var params: List<JsonElement>
)

@Serializable
internal data class JsonRpcError(var code: Int, var message: String)

@Serializable
internal data class JsonRpcResponse<Result>(
    var jsonrpc: String = "2.0",
    var id: Int,
    var result: Result? = null,
    var error: JsonRpcError? = null
)

internal fun JsonRpcRequest.validate(response: JsonRpcResponse<*>) {
    if (this.id != response.id) {
        throw Exception("JSON RPC response ID does not match request ID")
    }
}

internal class JsonRpcException(error: JsonRpcError) : Exception(error.message)
