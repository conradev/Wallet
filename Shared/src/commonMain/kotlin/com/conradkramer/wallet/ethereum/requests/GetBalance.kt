package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.BlockSpecifier
import com.conradkramer.wallet.ethereum.Quantity
import com.conradkramer.wallet.ethereum.Request

internal class GetBalance(val address: Address, val specifier: BlockSpecifier = BlockSpecifier.LATEST) : Request<Quantity>() {
    constructor(params: List<String>) : this(Address.fromString(params[0]), optional(params, 1, BlockSpecifier.Companion::fromString, BlockSpecifier.LATEST))

    override val method: String = "eth_getBalance"
    override val params: List<String>
        get() = listOf(address.toString(), specifier.encoded)

    companion object {
        fun <R> optional(params: List<String>, index: Int, constructor: (String) -> R, default: R): R {
            return if (index < params.size) {
                constructor(params[index])
            } else {
                default
            }
        }
    }

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
