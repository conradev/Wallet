package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.encoding.toByteArray
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.types.Address

internal class ERC165(client: RpcClient, address: Address) : Contract(client, address) {
    suspend fun supportsInterface(tag: UInt) = invoke(
        Invocation("supportsInterface")
            .parameter(Type.Bytes(4)) { encode(tag.toByteArray()) },
        Type.Bool::decode
    )
}

internal fun RpcClient.erc165(address: Address) = ERC165(this, address)
