package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.BlockSpecifier
import kotlinx.serialization.json.JsonElement

internal data class GetBalance(
    val address: Address,
    val specifier: BlockSpecifier = BlockSpecifier.LATEST
) : Request() {
    constructor(params: List<JsonElement>) : this(
        decode(params, 0),
        decode(params, 1, BlockSpecifier.LATEST)
    )

    override val method = Companion.method
    override val params: List<JsonElement>
        get() = listOf(encode(address), encode(specifier))

    companion object {
        val method = "eth_getBalance"
    }
}
