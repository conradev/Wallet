package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.BalanceStore
import com.conradkramer.wallet.DatabaseBalanceStore
import com.conradkramer.wallet.crypto.Keccak256Digest
import com.conradkramer.wallet.encoding.encodeHex
import com.conradkramer.wallet.ethereum.requests.Call
import com.conradkramer.wallet.ethereum.requests.GetBalance
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import mu.KLogger
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.EmptyCoroutineContext

internal interface BalanceUpdater {
    val address: Address

    fun update()
}

internal class RpcBalanceUpdater(
    override val address: Address,
    private val client: RpcClient,
    private val tokenStore: TokenStore,
    private val logger: KLogger,
) : KoinComponent, BalanceUpdater {
    private val scope = CoroutineScope(EmptyCoroutineContext)
    private val balanceStore: BalanceStore by lazy { getKoin().get<DatabaseBalanceStore> { parametersOf(address) } }

    private val prefix = Keccak256Digest
        .digest("balanceOf(address)".toByteArray())
        .encodeHex()
        .take(8)

    private suspend fun updateTokenBalance(token: Token) {
        try {
            val transaction = Transaction(
                to = token.contractAddress,
                data = Data.fromString(
                    "0x${prefix}000000000000000000000000${
                    address.toString().drop(2)
                    }"
                ),
            )
            val result = client.send<Quantity>(Call(transaction))
            balanceStore.add(token, result)
        } catch (e: Exception) {
            logger.error { e.stackTraceToString() }
            logger.error { e.stackTraceToString().substring(startIndex = 300) }
            logger.error { e.stackTraceToString().substring(startIndex = 600) }
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun updateTokenBalances() {
        flowOf(*tokenStore.tokens.value.values.toTypedArray())
            .flatMapMerge(2) {
                updateTokenBalance(it)
                flowOf(Unit)
            }
            .launchIn(scope)
            .join()
    }

    private suspend fun updateEthBalance() {
        val result = client.send<Quantity>(GetBalance(address))
        balanceStore.add(Ethereum, result)
    }

    override fun update() {
        scope.launch { updateEthBalance() }
        scope.launch { updateTokenBalances() }
    }
}
