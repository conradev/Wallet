package com.conradkramer.wallet

import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.Data
import com.conradkramer.wallet.ethereum.Quantity
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.Token
import com.conradkramer.wallet.ethereum.Transaction
import com.conradkramer.wallet.ethereum.requests.Call
import com.conradkramer.wallet.ethereum.requests.GetBalance
import com.conradkramer.wallet.platform.Keccak256Digest
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

internal interface BalanceUpdater {
    fun update(address: Address)
}

internal class EthereumBalanceUpdater(
    private val rpcClient: RpcClient,
    private val balanceStore: EthereumBalanceStore
) : BalanceUpdater {
    private val scope = CoroutineScope(EmptyCoroutineContext)

    private val balanceOfABI = Keccak256Digest().digest("balanceOf(address)".toByteArray()).encodeHex()
        .take(8)

    private suspend fun updateERC20Balances(address: Address) {
        val tokenRequests = Token.values()
            .map {
                val tx = Transaction(
                    to = it.contractAddress,
                    data = Data.fromString(
                        "0x${balanceOfABI}000000000000000000000000${
                            address.toString().drop(2)
                        }"
                    ),
                )

                Pair(it.contractAddress, Call<Quantity>(tx))
            }

        tokenRequests.forEach { (contractAddress, call) ->
            val result = rpcClient.request(call)

            Balance(address = address, contractAddress = contractAddress, quantity = result)
                .let { balanceStore.add(it) }
        }
    }

    private suspend fun updateEthBalance(address: Address) {
        val request = GetBalance(address)
        val result = rpcClient.request(request)

        Balance(address = address, contractAddress = null, quantity = result)
            .let { balanceStore.add(it) }
    }

    override fun update(address: Address) {
        scope.launch {
            updateEthBalance(address)
            updateERC20Balances(address)
        }
    }
}
