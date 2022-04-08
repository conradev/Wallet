package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.BlockSpecifier
import com.conradkramer.wallet.ethereum.Transaction
import kotlinx.serialization.json.JsonElement

internal data class Call(
    val transaction: Transaction,
    val specifier: BlockSpecifier = BlockSpecifier.LATEST
) : Request() {
    constructor(params: List<JsonElement>) : this(
        decode(params, 0),
        decode(params, 1, BlockSpecifier.LATEST)
    )

    override val method = Companion.method
    override val params: List<JsonElement>
        get() = listOf(encode(transaction), encode(specifier))

    companion object {
        val method = "eth_call"
    }
}
