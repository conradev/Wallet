package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.Data
import kotlinx.serialization.json.JsonElement

internal interface SignRequest {
    val address: Address
    val data: Data
}

internal data class Sign(
    override val address: Address,
    override val data: Data,
) : Request(), SignRequest {
    constructor(params: List<JsonElement>) : this(
        decode(params, 0),
        decode(params, 1),
    )

    override val method = Companion.method
    override val params: List<JsonElement>
        get() = listOf(encode(address), encode(data))

    companion object {
        const val method = "eth_sign"
    }
}
