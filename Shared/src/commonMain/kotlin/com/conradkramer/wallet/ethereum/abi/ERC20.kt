package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.RpcClient

internal class ERC20(client: RpcClient, address: Address) : Contract(client, address) {
    suspend fun totalSupply() = invoke(Invocation("totalSupply"), Type.UInt()::decode)
    suspend fun name() = invoke(Invocation("name"), Type.String::decode)
    suspend fun symbol() = invoke(Invocation("symbol"), Type.String::decode)
    suspend fun decimals() = invoke(Invocation("decimals"), Type.UInt(8)::decodeInt)
}

internal fun RpcClient.erc20(address: Address) = ERC20(this, address)
