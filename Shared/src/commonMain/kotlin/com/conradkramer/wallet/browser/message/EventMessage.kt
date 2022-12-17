package com.conradkramer.wallet.browser.message

import com.conradkramer.wallet.browser.event.Event
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.random.Random

@Serializable
@SerialName("event")
internal data class EventMessage protected constructor(
    override val id: Long,
    override val session: Session,
    val payload: Payload
) : Message() {
    constructor(session: Session, event: Event) : this(Random.nextLong(), session, event)
    constructor(id: Long, session: Session, event: Event) : this(id, session, Payload(event.name, event.value))

    @Serializable
    internal data class Payload(
        val name: String,
        val value: JsonElement
    )
}
