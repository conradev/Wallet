package com.conradkramer.wallet.browser.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("start_session")
internal data class StartSessionMessage(
    override val id: Long,
    override val url: String,
    override val frame: Frame,
    override val session: Session,
    val payload: Payload
) : RequestMessage() {
    @Serializable
    internal class Payload
}
