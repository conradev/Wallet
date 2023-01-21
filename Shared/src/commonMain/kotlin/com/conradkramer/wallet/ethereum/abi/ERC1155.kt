package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.types.Address

internal class ERC1155(client: RpcClient, address: Address) : Interface(client, address) {
    override val interfaceId: UInt = 0xD9B67A26u
}

internal suspend fun RpcClient.erc1155(address: Address) = Interface.ifSupported(ERC1155(this, address))
