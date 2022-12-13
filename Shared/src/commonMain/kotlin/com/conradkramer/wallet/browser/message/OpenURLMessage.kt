package com.conradkramer.wallet.browser.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
@SerialName("open_url")
internal data class OpenURLMessage(
    override val id: Long,
    override val session: Session,
    val url: Payload
) : Message() {
    constructor(session: Session, url: String) : this(
        Random.nextLong(),
        session,
        Payload(url)
    )

    @Serializable
    internal data class Payload(
        val url: String
    )
}
