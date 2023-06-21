package com.conradkramer.wallet.browser.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
@SerialName("open_url")
internal data class OpenURLMessage protected constructor(
    override val id: Long,
    override val session: Session,
    val payload: Payload,
) : Message() {
    constructor(id: Long, session: Session, url: String) : this(id, session, Payload(url))
    constructor(session: Session, url: String) : this(Random.nextLong(), session, url)

    @Serializable
    internal data class Payload(
        val url: String,
    )
}
