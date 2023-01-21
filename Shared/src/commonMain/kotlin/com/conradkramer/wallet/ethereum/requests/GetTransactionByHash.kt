package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.ethereum.types.Quantity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

internal data class GetTransactionByHash(
    val hash: Data
) : Request() {
    constructor(params: List<JsonElement>) : this(
        decode<Data>(params, 0)
    )

    override val method = Companion.method
    override val params: List<JsonElement>
        get() = listOf(encode(hash))

    companion object {
        const val method = "eth_getTransactionByHash"
    }
}

@Serializable
internal data class CompletedTransaction(
    val chainId: Chain,
    val blockHash: Data,
    val blockNumber: Quantity,
    val transactionIndex: Quantity,
    val hash: Data,
    val from: Address,
    val to: Address?,
    val gas: Quantity,
    val gasPrice: Quantity,
    val input: Data,
    val value: Quantity,
    val nonce: Quantity,
    val r: Quantity,
    val s: Quantity,
    val v: Quantity
)
