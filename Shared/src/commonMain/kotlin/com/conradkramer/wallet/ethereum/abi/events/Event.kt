package com.conradkramer.wallet.ethereum.abi.events

import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.Data

internal sealed class Event {
    companion object {
        fun from(address: Address, topics: List<Data>, data: Data): Event {
            val signature = topics.firstOrNull() ?: return AnyEvent(address, topics, data)
            val constructor: (address: Address, topics: List<Data>, data: Data) -> Event = when (signature.data) {
                Transfer.selector.data -> ::Transfer
                else -> ::AnyEvent
            }
            return constructor(address, topics, data)
        }

        inline fun <reified T> decode(
            params: List<Data>,
            index: Int,
            map: (ByteArray) -> T,
        ): T {
            return if (index < params.size) {
                map(params[index].data)
            } else {
                throw Exception("Argument at index $index is missing")
            }
        }
    }
}

internal class AnyEvent(
    val address: Address,
    val topics: List<Data>,
    val data: Data,
) : Event()
