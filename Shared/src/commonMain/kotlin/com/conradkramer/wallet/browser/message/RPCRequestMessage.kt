package com.conradkramer.wallet.browser.message

import com.conradkramer.wallet.ethereum.requests.Request
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
@SerialName("rpc_request")
internal data class RPCRequestMessage protected constructor(
    override val id: Long,
    override val url: String,
    override val frame: Frame,
    override val session: Session,
    val payload: Payload,
) : RequestMessage() {
    constructor(id: Long, url: String, frame: Frame, session: Session, request: Request) : this(
        id,
        url,
        frame,
        session,
        Payload(request.method, request.params),
    )

    @Serializable
    internal data class Payload(
        val method: String,
        val params: List<JsonElement>? = null,
    )

    val request: Request
        get() = Request.fromMethodAndParams(payload.method, payload.params)
}
