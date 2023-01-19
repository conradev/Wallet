package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.bigint.BigInteger
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.types.Address

internal class ERC721Metadata(client: RpcClient, address: Address) : Contract(client, address) {
    suspend fun name() = invoke(Invocation("name"), Type.String::decode)
    suspend fun symbol() = invoke(Invocation("symbol"), Type.String::decode)

    suspend fun tokenURI(tokenId: BigInteger) = invoke(
        Invocation("tokenURI").parameter(Type.UInt()) { encode(tokenId) },
        Type.String::decode
    )
}

internal class ERC721Enumerable(client: RpcClient, address: Address) : Contract(client, address) {
    suspend fun totalSupply() = invoke(Invocation("totalSupply"), Type.UInt()::decode)
}

internal fun RpcClient.erc721Metadata(address: Address) = ERC721Metadata(this, address)
internal fun RpcClient.erc721Enumerable(address: Address) = ERC721Enumerable(this, address)
