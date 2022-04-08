package com.conradkramer.wallet.browser.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.random.Random

@Serializable
@SerialName("rpc_response")
internal data class RPCResponseMessage(
    override val id: Long,
    override val frame: Frame,
    @SerialName("frame_id")
    override val frameId: String,
    @SerialName("browser_pid")
    override val browserPid: Int,
    val payload: Payload
) : Message() {
    constructor(message: RPCRequestMessage, result: JsonElement) : this(
        Random.nextLong(),
        message.frame,
        message.frameId,
        message.browserPid,
        Payload(message.id, result)
    )

    @Serializable
    internal data class Payload(
        @SerialName("request_id")
        val requestId: Long,
        val result: JsonElement
    )
}
