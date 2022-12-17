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
    constructor(id: Long, url: String, frame: Frame, session: Session) : this(id, url, frame, session, Payload())

    @Serializable
    internal class Payload {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }
}
