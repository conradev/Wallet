package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.types.Data
import kotlinx.serialization.json.JsonElement

internal data class SHA3(
    val data: Data,
) : Request() {
    constructor(params: List<JsonElement>) : this(
        decode<Data>(params, 0),
    )

    override val method = Companion.method
    override val params: List<JsonElement>
        get() = listOf(encode(data))

    companion object {
        const val method = "web3_sha3"
    }
}
