package com.conradkramer.wallet.browser.message

import com.conradkramer.wallet.ethereum.requests.Request
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
@SerialName("rpc_request")
internal data class RPCRequestMessage(
    override val id: Long,
    override val frame: Frame,
    @SerialName("frame_id")
    override val frameId: String,
    override val url: String,
    @SerialName("browser_pid")
    override val browserPid: Int,
    val payload: Payload
) : RequestMessage() {
    @Serializable
    internal data class Payload(
        val method: String,
        val params: List<JsonElement>? = null
    )

    val request: Request
        get() = Request.fromMethodAndParams(payload.method, payload.params)
}
