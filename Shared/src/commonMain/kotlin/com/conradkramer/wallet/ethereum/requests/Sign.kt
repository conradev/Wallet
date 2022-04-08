package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.Data
import kotlinx.serialization.json.JsonElement

internal data class Sign(
    val address: Address,
    val data: Data
) : Request() {
    constructor(params: List<JsonElement>) : this(
        decode(params, 0),
        decode(params, 1)
    )

    override val method = Companion.method
    override val params: List<JsonElement>
        get() = listOf(encode(address), encode(data))

    companion object {
        val method = "eth_sign"
    }
}
