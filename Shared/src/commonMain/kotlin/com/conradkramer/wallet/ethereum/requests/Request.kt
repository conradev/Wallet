package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.requests.alchemy.AssetTransfers
import com.conradkramer.wallet.ethereum.requests.alchemy.GetAssetTransfers
import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.BlockSpecifier
import com.conradkramer.wallet.ethereum.types.BlockTag
import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.ethereum.types.Quantity
import com.conradkramer.wallet.ethereum.types.Transaction
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
            value: JsonElement
        ): T {
            return json.decodeFromJsonElement(value)
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
                Accounts.method -> ::Accounts
                Call.method -> ::Call
                ChainId.method -> ::ChainId
                ClientVersion.method -> ::ClientVersion
                GasPrice.method -> ::GasPrice
                GetAssetTransfers.method -> ::GetAssetTransfers
                GetBalance.method -> ::GetBalance
                GetBlockByNumber.method -> ::GetBlockByNumber
                GetPermissions.method -> ::GetPermissions
                GetTransactionByHash.method -> ::GetTransactionByHash
                GetTransactionReceipt.method -> ::GetTransactionReceipt
                PersonalSign.method -> ::PersonalSign
                RequestAccounts.method -> ::RequestAccounts
                RequestPermissions.method -> ::RequestPermissions
                SendTransaction.method -> ::SendTransaction
                SHA3.method -> ::SHA3
                Sign.method -> ::Sign
                SignTransaction.method -> ::SignTransaction
                SignTypedData.method -> ::SignTypedData
                Subscribe.method -> ::Subscribe
                else -> { it -> AnyRequest(method, it) }
            }
            return constructor(params ?: emptyList())
        }

        @OptIn(ExperimentalSerializationApi::class)
        val json = Json {
            encodeDefaults = true
            explicitNulls = false
            ignoreUnknownKeys = true
            serializersModule = SerializersModule {
                include(serializersModuleOf(Address.serializer()))
                include(serializersModuleOf(Chain.serializer()))
                include(serializersModuleOf(Quantity.serializer()))
                include(serializersModuleOf(Data.serializer()))
                include(serializersModuleOf(BlockSpecifier.serializer()))
                include(serializersModuleOf(AssetTransfers.serializer()))
                include(serializersModuleOf(AssetTransfers.Transfer.serializer()))
                include(serializersModuleOf(Receipt.serializer()))
                include(serializersModuleOf(Log.serializer()))
                include(serializersModuleOf(BlockTag.serializer()))
                include(serializersModuleOf(Transaction.serializer()))
                include(serializersModuleOf(CompletedTransaction.serializer()))
                include(serializersModuleOf(JsonRpcRequest.serializer()))
                include(serializersModuleOf(JsonRpcResponse.serializer()))
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
internal data class JsonRpcError(
    val code: Int,
    override val message: String,
    val data: JsonElement? = null
) : Throwable(message)

@Serializable
internal data class JsonRpcResponse(
    var jsonrpc: String = "2.0",
    var id: Int,
    var result: JsonElement? = null,
    var error: JsonRpcError? = null
)

internal fun JsonRpcRequest.validate(response: JsonRpcResponse) {
    if (this.id != response.id) {
        throw Exception("JSON RPC response ID does not match request ID")
    }
}
