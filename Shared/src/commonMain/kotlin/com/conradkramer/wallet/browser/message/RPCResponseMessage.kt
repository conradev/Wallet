package com.conradkramer.wallet.browser.message

import com.conradkramer.wallet.ethereum.requests.JsonRpcError
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.random.Random

@Serializable
@SerialName("rpc_response")
internal data class RPCResponseMessage(
    override val id: Long,
    override val session: Session,
    val payload: Payload,
) : Message() {
    constructor(id: Long, message: RPCRequestMessage, result: JsonElement) : this(
        id,
        message.session,
        Payload(message.id, result, null),
    )
    constructor(id: Long, message: RPCRequestMessage, error: JsonRpcError) : this(
        id,
        message.session,
        Payload(message.id, null, error),
    )

    constructor(message: RPCRequestMessage, result: JsonElement) : this(Random.nextLong(), message, result)
    constructor(message: RPCRequestMessage, error: JsonRpcError) : this(Random.nextLong(), message, error)

    @Serializable
    internal data class Payload(
        @SerialName("request_id")
        val requestId: Long,
        val result: JsonElement? = null,
        val error: JsonRpcError? = null,
    )

    @Serializable
    internal data class RPCError(
        val code: Int,
        override val message: String,
        val data: JsonElement? = null,
    ) : Throwable(message)
}
