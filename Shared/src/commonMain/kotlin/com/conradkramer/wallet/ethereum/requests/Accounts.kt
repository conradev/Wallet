package com.conradkramer.wallet.ethereum.requests

import kotlinx.serialization.json.JsonElement

internal class Accounts() : Request() {
    @Suppress("UNUSED_PARAMETER")
    constructor(params: List<JsonElement>) : this()

    override val method = Companion.method
    override val params: List<JsonElement> = listOf()

    companion object {
        val method = "eth_accounts"
    }
}
