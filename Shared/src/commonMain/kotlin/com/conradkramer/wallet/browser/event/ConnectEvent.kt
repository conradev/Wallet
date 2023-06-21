package com.conradkramer.wallet.browser.event

import com.conradkramer.wallet.ethereum.types.Quantity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class ConnectEvent(
    val chainId: Quantity,
) : Event() {
    override val name = "connect"

    override val value: JsonElement
        get() = encode(chainId)
}
