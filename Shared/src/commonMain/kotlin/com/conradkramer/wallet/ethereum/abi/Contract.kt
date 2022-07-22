package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.Data
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.Transaction
import com.conradkramer.wallet.ethereum.requests.Call

internal abstract class Contract(
    val client: RpcClient,
    val address: Address
) {
    protected suspend inline fun <reified T> invoke(invocation: Invocation): T {
        return client.send(
            Call(
                Transaction(
                    to = address,
                    data = Data(invocation.build())
                )
            )
        )
    }
}
