package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.bigint.BigInteger
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.types.Address

internal class ERC721(client: RpcClient, address: Address) : Interface(client, address) {
    override val interfaceId: UInt = 0x80AC58CDu

    suspend fun ownerOf(token: BigInteger) = invoke(
        Invocation("ownerOf").parameter(Type.UInt()) { encode(token) },
        Type.UInt(160)::decode,
    )
}

internal class ERC721Metadata(client: RpcClient, address: Address) : Interface(client, address) {
    override val interfaceId: UInt = 0x5B5E139Fu
    suspend fun name() = invoke(Invocation("name"), Type.String::decode)
    suspend fun symbol() = invoke(Invocation("symbol"), Type.String::decode)

    suspend fun tokenURI(tokenId: BigInteger) = invoke(
        Invocation("tokenURI").parameter(Type.UInt()) { encode(tokenId) },
        Type.String::decode,
    )
}

internal class ERC721Enumerable(client: RpcClient, address: Address) : Interface(client, address) {
    override val interfaceId: UInt = 0x780E9D63u
    suspend fun totalSupply() = invoke(Invocation("totalSupply"), Type.UInt()::decode)
}

internal suspend fun RpcClient.erc721(address: Address) = Interface.ifSupported(ERC721(this, address))
internal suspend fun RpcClient.erc721Metadata(address: Address) = Interface.ifSupported(ERC721Metadata(this, address))
internal suspend fun RpcClient.erc721Enumerable(address: Address) = Interface.ifSupported(
    ERC721Enumerable(this, address),
)
