package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.ethereum.requests.GetBalance
import kotlinx.serialization.Serializable

internal abstract class Request<Response> {
    abstract val method: String
    abstract val params: List<String>

    fun jsonRpcRequest(id: Int): JsonRpcRequest {
        return JsonRpcRequest(method = method, params = params, id = id)
    }

    companion object {
        private val mapping: Map<String, (List<String>) -> Request<*>> = mapOf(
            ("eth_getBalance" to ::GetBalance)
        )

        fun fromRequest(request: JsonRpcRequest): Request<*> {
            val constructor = mapping[request.method]
                ?: throw Exception("Unknown request method")

            return constructor(request.params)
        }
    }
}

@Serializable
internal data class JsonRpcRequest(var jsonrpc: String = "2.0", var method: String, var id: Int = 0, var params: List<String>)

@Serializable
internal data class JsonRpcError(var code: Int, var message: String)

@Serializable
internal data class JsonRpcResponse<Result>(var jsonrpc: String = "2.0", var id: Int = 0, var result: Result? = null, var error: JsonRpcError? = null)

internal fun JsonRpcRequest.validate(response: JsonRpcResponse<*>) {
    if (this.id != response.id) {
        throw Exception("JSON RPC response ID does not match request ID")
    }
}

internal class JsonRpcException(error: JsonRpcError) : Exception(error.message)
