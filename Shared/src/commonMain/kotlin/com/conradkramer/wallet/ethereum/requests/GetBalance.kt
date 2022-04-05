package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.BlockSpecifier
import com.conradkramer.wallet.ethereum.Quantity
import com.conradkramer.wallet.ethereum.Request
import kotlinx.serialization.json.JsonElement

internal data class GetBalance(val address: Address, val specifier: BlockSpecifier = BlockSpecifier.LATEST) : Request<Quantity>() {
    constructor(params: List<JsonElement>) : this(
        decode(params, 0, Address.serializer()),
        decode(params, 1, BlockSpecifier.LATEST, BlockSpecifier.serializer())
    )

    override val method: String = "eth_getBalance"
    override val params: List<JsonElement>
        get() = listOf(encode(address), encode(specifier))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GetBalance

        if (address != other.address) return false
        if (specifier != other.specifier) return false
        if (method != other.method) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + specifier.hashCode()
        result = 31 * result + method.hashCode()
        return result
    }
}
