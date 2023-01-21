package com.conradkramer.wallet.ethereum.abi.events

import com.conradkramer.wallet.ethereum.abi.Selector
import com.conradkramer.wallet.ethereum.abi.Type
import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.ethereum.types.Quantity

internal class Transfer private constructor(
    val address: Address,
    val from: Address,
    val to: Address,
    val value: Quantity
) : Event() {
    constructor(address: Address, topics: List<Data>, @Suppress("UNUSED_PARAMETER") data: Data) : this(
        address,
        decode(topics, 1, ::Address),
        decode(topics, 2, ::Address),
        decode(topics, 3, ::Quantity)
    )

    companion object {
        val selector = Selector("Transfer", Type.Address, Type.Address, Type.UInt())
    }
}
