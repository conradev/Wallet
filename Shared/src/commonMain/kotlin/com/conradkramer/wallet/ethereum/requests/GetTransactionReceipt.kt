package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.ethereum.types.Quantity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

internal data class GetTransactionReceipt(
    val hash: Data,
) : Request() {
    constructor(params: List<JsonElement>) : this(
        decode<Data>(params, 0),
    )

    override val method = Companion.method
    override val params: List<JsonElement>
        get() = listOf(encode(hash))

    companion object {
        const val method = "eth_getTransactionReceipt"
    }
}

@Serializable
internal data class Receipt(
    val transactionHash: Data,
    val status: Quantity,
    val contractAddress: Address?,
    val gasUsed: Quantity,
    val logs: List<Log>,
)

@Serializable
internal data class Log(
    val transactionHash: Data,
    val logIndex: Quantity,
    val address: Address,
    val topics: List<Data>,
    val data: Data,
    val removed: Boolean,
)
