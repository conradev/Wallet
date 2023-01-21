package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.BlockSpecifier

internal class ERC20(client: RpcClient, address: Address) : Interface(client, address) {
    override val interfaceId: UInt = 0x36372B07u
    suspend fun totalSupply() = invoke(Invocation("totalSupply"), Type.UInt()::decode)
    suspend fun name() = invoke(Invocation("name"), Type.String::decode)
    suspend fun symbol() = invoke(Invocation("symbol"), Type.String::decode)
    suspend fun decimals() = invoke(Invocation("decimals"), Type.UInt(8)::decodeInt)

    suspend fun balanceOf(address: Address, block: BlockSpecifier = BlockSpecifier.LATEST) = invoke(
        Invocation("balanceOf")
            .parameter(Type.Address) { encode(address) },
        Type.UInt()::decode,
        block
    )
}

internal fun RpcClient.erc20(address: Address) = ERC20(this, address)
