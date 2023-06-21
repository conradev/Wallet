package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.BlockSpecifier
import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.ethereum.types.Quantity
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement

internal data class GetBlockByNumber(
    val block: BlockSpecifier = BlockSpecifier.LATEST,
    val hydrated: Boolean,
) : Request() {
    constructor(params: List<JsonElement>) : this(
        decode(params, 0, BlockSpecifier.LATEST),
        decode(params, 1),
    )

    override val method = Companion.method
    override val params: List<JsonElement>
        get() = listOf(encode(block), encode(hydrated))

    companion object {
        val method = "eth_getBlockByNumber"
    }
}

@Serializable
internal data class Block<Tx>(
    val number: Quantity,
    val timestamp: Quantity,
    val logsBloom: Data,
    val transactions: List<Tx>,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
@SerialName("0x2")
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
    val maxFeePerGas: Quantity?,
    val maxPriorityFeePerGas: Quantity?,
)
