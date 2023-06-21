package com.conradkramer.wallet.ethereum.requests

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray

internal data class Subscribe(
    val events: Set<String>,
) : Request() {
    constructor(params: List<JsonElement>) : this(params.map { decode<String>(it) }.toSet())

    override val method = Companion.method
    override val params: List<JsonElement>
        get() = encode(events).jsonArray

    companion object {
        const val method = "eth_subscribe"
    }
}
