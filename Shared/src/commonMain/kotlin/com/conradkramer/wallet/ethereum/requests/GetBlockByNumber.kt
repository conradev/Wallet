package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.BlockSpecifier
import com.conradkramer.wallet.ethereum.Data
import com.conradkramer.wallet.ethereum.Quantity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

internal data class GetBlockByNumber(
    val block: BlockSpecifier = BlockSpecifier.LATEST,
    val hydrated: Boolean
) : Request() {
    constructor(params: List<JsonElement>) : this(
        decode(params, 0, BlockSpecifier.LATEST),
        decode(params, 1)
    )

    override val method = Companion.method
    override val params: List<JsonElement>
        get() = listOf(encode(block), encode(hydrated))

    companion object {
        val method = "eth_getBlockByNumber"
    }
}

@Serializable
internal data class HydratedBlock(
    val number: Quantity,
    val timestamp: Quantity,
    val logsBloom: Data,
    val transactions: List<Transaction>
)

@Serializable
internal data class Transaction(
    val blockNumber: Quantity,
    val transactionIndex: Quantity,
    val hash: Data,
    val input: Data,
    val from: Address,
    val to: Address?,
    val value: Quantity,
    val gas: Quantity,
    val gasPrice: Quantity,
    val maxFeePerGas: Quantity,
    val maxPriorityFeePerGas: Quantity
)
