package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.types.Transaction
import kotlinx.serialization.json.JsonElement

internal data class SignTransaction(
    val transaction: Transaction
) : Request() {
    constructor(params: List<JsonElement>) : this(
        decode<Transaction>(params, 0)
    )

    override val method = Companion.method
    override val params: List<JsonElement>
        get() = listOf(encode(transaction))

    companion object {
        const val method = "eth_signTransaction"
    }
}
