package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.encoding.toByteArray
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.types.Address
import io.ktor.utils.io.core.ByteOrder

internal class ERC165(client: RpcClient, address: Address) : Interface(client, address) {
    override val interfaceId: UInt = 0x01FFC9A7u

    suspend fun supportsInterface(tag: UInt) = invoke(
        Invocation("supportsInterface")
            .parameter(Type.Bytes(4)) { encode(tag.toByteArray(ByteOrder.BIG_ENDIAN)) },
        Type.Bool::decode,
    )

    suspend fun ifSupported(): ERC165? {
        val supported = runCatching { supportsInterface(interfaceId) && !supportsInterface(0xFFFFFFFFu) }
            .getOrNull() ?: false
        return if (supported) this else null
    }
}

internal suspend fun RpcClient.erc165(address: Address) = ERC165(this, address).ifSupported()
