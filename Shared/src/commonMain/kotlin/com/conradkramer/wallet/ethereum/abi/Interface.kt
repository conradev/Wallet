package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.requests.Call
import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.BlockSpecifier
import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.ethereum.types.Transaction

internal abstract class Interface(
    val client: RpcClient,
    val address: Address,
) {
    abstract val interfaceId: UInt

    protected suspend inline fun <T> invoke(
        invocation: Invocation,
        decode: (ByteArray) -> T,
        block: BlockSpecifier = BlockSpecifier.LATEST,
    ): T {
        val result: Data = client.send(
            Call(
                Transaction(
                    to = address,
                    data = Data(invocation.build()),
                ),
                block,
            ),
        )
        return decode(result.data)
    }

    companion object {
        suspend fun <T : Interface> ifSupported(instance: T): T? {
            val supports = ERC165(instance.client, instance.address).runCatching {
                supportsInterface(
                    instance.interfaceId,
                )
            }
                .getOrNull() ?: false
            return if (supports) instance else null
        }
    }
}
