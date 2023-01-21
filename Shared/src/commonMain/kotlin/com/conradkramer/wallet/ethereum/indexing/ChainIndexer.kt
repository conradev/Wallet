@file:OptIn(ExperimentalCoroutinesApi::class)

package com.conradkramer.wallet.ethereum.indexing

import com.conradkramer.wallet.AccountStore
import com.conradkramer.wallet.ethereum.AlchemyProvider
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.RpcProvider
import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.logger
import com.conradkramer.wallet.sql.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import mu.KLogger
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import kotlin.coroutines.EmptyCoroutineContext

internal class ChainIndexer(
    val chain: Chain,
    private val database: Database,
    private val provider: RpcProvider,
    private val accountStore: AccountStore,
    private val logger: KLogger
) : KoinComponent {
    private val scope: CoroutineScope = CoroutineScope(
        EmptyCoroutineContext + Dispatchers.Default.limitedParallelism(1)
    )

    private val client = RpcClient(provider.endpointUrl(chain), getKoin().logger(named("IndexerRpcClient")))
    private val transactionIndexer = TransactionIndexer(client, database, chain, getKoin().logger<TransactionIndexer>())
    private val receiptIndexer = ReceiptIndexer(client, database, chain, getKoin().logger<ReceiptIndexer>())
    private val erc20ContractIndexer = ERC20ContractIndexer(client, database, chain, getKoin().logger<ReceiptIndexer>())
    private val erc721ContractIndexer = ERC721ContractIndexer(
        client,
        database,
        chain,
        getKoin().logger<ReceiptIndexer>()
    )
    private val addressIndexers: MutableMap<Address, Set<Indexer>> = mutableMapOf()

    init {
        scope.launch {
            accountStore
                .accounts
                .map { account -> account.map { it.ethereumAddress } }
                .distinctUntilChanged()
                .collect { updateAddressIndexers() }
        }
    }

    private fun updateAddressIndexers() {
        val current = addressIndexers.keys
        val updated = accountStore.accounts.value.map { it.ethereumAddress }.toSet()

        current.subtract(updated).forEach { addressIndexers.remove(it) }

        updated.subtract(current).forEach { address ->
            val alchemyProvider = getKoin().get<AlchemyProvider>()
            val alchemyClient = RpcClient(alchemyProvider.endpointUrl(chain), getKoin().logger(named("AlchemyClient")))

            addressIndexers[address] = setOf(
                AccountTransactionIndexer(
                    address,
                    alchemyClient,
                    database,
                    chain,
                    getKoin().logger<AccountTransactionIndexer>()
                ),
                BalanceIndexer(address, client, database, chain, getKoin().logger<BalanceIndexer>())
            )
        }
    }
}
