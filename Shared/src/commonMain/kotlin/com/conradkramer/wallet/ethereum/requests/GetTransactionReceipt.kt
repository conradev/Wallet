package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.Data
import com.conradkramer.wallet.ethereum.Quantity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

internal data class GetTransactionReceipt(
    val hash: Data
) : Request() {
    constructor(params: List<JsonElement>) : this(
        decode<Data>(params, 0)
    )

    override val method = Companion.method
    override val params: List<JsonElement>
        get() = listOf(encode(hash))

    companion object {
        val method = "eth_getTransactionReceipt"
    }
}

@Serializable
internal data class Receipt(
    val status: Quantity?,
    val contractAddress: Address?,
    val logs: List<Log>
)

@Serializable
internal data class Log(
    val removed: Boolean,
    val blockNumber: Quantity,
    val transactionIndex: Quantity,
    val logIndex: Quantity,
    val address: Address,
    val topics: List<Data>,
    val data: Data
)
