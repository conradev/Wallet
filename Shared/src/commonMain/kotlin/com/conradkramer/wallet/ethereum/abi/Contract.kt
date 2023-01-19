package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.requests.Call
import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.ethereum.types.Transaction

internal abstract class Contract(
    val client: RpcClient,
    val address: Address
) {
    protected suspend inline fun <T> invoke(invocation: Invocation, decode: (ByteArray) -> T): T {
        val result: Data = client.send(
            Call(
                Transaction(
                    to = address,
                    data = Data(invocation.build())
                )
            )
        )
        return decode(result.data)
    }
}
