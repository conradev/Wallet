package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.BlockSpecifier
import com.conradkramer.wallet.ethereum.BlockTag
import com.conradkramer.wallet.ethereum.Data
import com.conradkramer.wallet.ethereum.Quantity
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.serializersModuleOf

internal abstract class Request {
    abstract val method: String
    abstract val params: List<JsonElement>

    fun jsonRpcRequest(id: Int): JsonRpcRequest {
        return JsonRpcRequest(method = method, params = params, id = id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Request

        if (method != other.method) return false
        if (params != other.params) return false

        return true
    }

    override fun hashCode(): Int {
        var result = method.hashCode()
        result = 31 * result + params.hashCode()
        return result
    }

    companion object {
        inline fun <reified T> encode(value: T): JsonElement {
            return json.encodeToJsonElement(value)
        }

        inline fun <reified T> decode(
            params: List<JsonElement>,
            index: Int
        ): T {
            return json.decodeFromJsonElement(params[index])
        }

        inline fun <reified T> decode(
            params: List<JsonElement>,
            index: Int,
            default: T
        ): T {
            return if (index < params.size) {
                decode(params, index)
            } else {
                default
            }
        }

        fun fromMethodAndParams(method: String, params: List<JsonElement>?): Request {
            val constructor: (List<JsonElement>) -> Request = when (method) {
                GetBalance.method -> ::GetBalance
                Call.method -> ::Call
                Accounts.method -> ::Accounts
                Sign.method -> ::Sign
                "eth_requestAccounts" -> ::Accounts
                else -> { it -> AnyRequest(method, it) }
            }
            return constructor(params ?: listOf())
        }

        @OptIn(ExperimentalSerializationApi::class)
        val json = Json {
            encodeDefaults = true
            explicitNulls = false
            serializersModule = SerializersModule {
                include(serializersModuleOf(Address.serializer()))
                include(serializersModuleOf(Quantity.serializer()))
                include(serializersModuleOf(Data.serializer()))
                include(serializersModuleOf(BlockSpecifier.serializer()))
                include(serializersModuleOf(BlockTag.serializer()))
                include(serializersModuleOf(JsonRpcRequest.serializer()))
            }
        }
    }
}

internal data class AnyRequest(
    override val method: String,
    override val params: List<JsonElement>
) : Request()

@Serializable
internal data class JsonRpcRequest(
    var jsonrpc: String = "2.0",
    var method: String,
    var id: Int,
    var params: List<JsonElement>?
) {
    val request: Request
        get() = Request.fromMethodAndParams(method, params ?: listOf())
}

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
