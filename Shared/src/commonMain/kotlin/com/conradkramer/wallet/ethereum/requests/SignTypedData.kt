package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.Address
import kotlinx.serialization.json.JsonElement

internal data class SignTypedData(
    val address: Address,
    val data: JsonElement
) : Request() {
    constructor(params: List<JsonElement>) : this(
        decode(params, 0),
        decode(params, 1)
    )

    override val method = Companion.method
    override val params: List<JsonElement>
        get() = listOf(encode(address), encode(data))

    companion object {
        const val method = "eth_signTypedData"
    }
}
