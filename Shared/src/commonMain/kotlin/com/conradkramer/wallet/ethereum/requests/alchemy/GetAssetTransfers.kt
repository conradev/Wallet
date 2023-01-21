package com.conradkramer.wallet.ethereum.requests.alchemy

import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.requests.Request
import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.BlockSpecifier
import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.ethereum.types.Quantity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

internal class GetAssetTransfers private constructor(
    private val payload: Payload
) : Request() {
    constructor(
        fromBlock: BlockSpecifier? = null,
        toBlock: BlockSpecifier? = null,
        fromAddress: Address? = null,
        toAddress: Address? = null,
        ascending: Boolean = false
    ) : this(
        Payload(
            fromBlock,
            toBlock,
            fromAddress,
            toAddress,
            if (ascending) "asc" else "desc"
        )
    )

    @Serializable
    private data class Payload(
        val fromBlock: BlockSpecifier?,
        val toBlock: BlockSpecifier?,
        val fromAddress: Address?,
        val toAddress: Address?,
        val order: String,
        val category: List<String> = listOf("external", "internal", "erc20", "erc721", "erc1155", "specialnft"),
        val excludeZeroValue: Boolean = false,
        var pageKey: String? = null
    )

    constructor(params: List<JsonElement>) : this(
        decode<Payload>(params, 0)
    )

    override val method = Companion.method
    override val params: List<JsonElement>
        get() = listOf(encode(payload))

    var pageKey: String?
        get() = payload.pageKey
        set(value) { payload.pageKey = value }

    companion object {
        val method = "alchemy_getAssetTransfers"
    }
}

@Serializable
internal data class AssetTransfers(
    val transfers: List<Transfer>,
    val pageKey: String?
) {
    @Serializable
    internal data class Transfer(
        @SerialName("blockNum")
        val block: Quantity,
        val hash: Data
    )
}

internal suspend fun RpcClient.all(request: GetAssetTransfers): List<AssetTransfers.Transfer> {
    val transfers = mutableListOf<AssetTransfers.Transfer>()
    var pageKey: String? = null
    do {
        val result: AssetTransfers = send(request.also { it.pageKey = pageKey })
        transfers.addAll(result.transfers)
        pageKey = result.pageKey
    } while (pageKey != null)
    return transfers
}
