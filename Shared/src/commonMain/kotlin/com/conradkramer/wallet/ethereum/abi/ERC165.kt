package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.encoding.toByteArray
import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.Quantity
import com.conradkramer.wallet.ethereum.RpcClient

internal class ERC165(client: RpcClient, address: Address) : Contract(client, address) {
    suspend fun supportsInterface(tag: UInt): Boolean {
        val invocation = Invocation("supportsInterface")
            .parameter(Type.Bytes(4)) { encode(tag.toByteArray()) }
        val result: Quantity = this.invoke(invocation)
        return result.toLong() != 0L
    }
}

internal fun RpcClient.erc165(address: Address) = ERC165(this, address)
