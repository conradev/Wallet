package com.conradkramer.wallet.ethereum.requests

import kotlinx.serialization.json.JsonElement

internal class ChainId() : Request() {
    @Suppress("UNUSED_PARAMETER")
    constructor(params: List<JsonElement>) : this()

    override val method = Companion.method
    override val params: List<JsonElement> = emptyList()

    companion object {
        const val method = "eth_chainId"
    }
}
