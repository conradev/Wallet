package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.BlockSpecifier
import com.conradkramer.wallet.ethereum.Data
import com.conradkramer.wallet.ethereum.Request
import com.conradkramer.wallet.ethereum.Transaction
import kotlinx.serialization.json.JsonElement

internal data class Call<T>(val transaction: Transaction, val specifier: BlockSpecifier = BlockSpecifier.LATEST) : Request<T>() {
    constructor(params: List<JsonElement>) : this(
        decode(params, 0, Transaction.serializer()),
        decode(params, 1, BlockSpecifier.LATEST, BlockSpecifier.serializer())
    )

    override val method: String = "eth_call"
    override val params: List<JsonElement>
        get() = listOf(encode(transaction), encode(specifier))
}
