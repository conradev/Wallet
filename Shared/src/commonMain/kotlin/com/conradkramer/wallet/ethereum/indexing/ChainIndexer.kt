package com.conradkramer.wallet.ethereum.indexing

import com.conradkramer.wallet.AccountStore
import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.sql.Database
import io.ktor.http.parametersOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.parametersOf

internal class ChainIndexer(
    val chain: Chain,
    private val scope: CoroutineScope,
    private val database: Database,
    private val accountStore: AccountStore
) : KoinComponent {
    private val transactionIndexer: TransactionIndexer = getKoin().get { parametersOf(chain, scope) }
    private val receiptIndexer: ReceiptIndexer = getKoin().get { parametersOf(chain, scope) }
    private val erc20ContractIndexer: ERC20ContractIndexer = getKoin().get { parametersOf(chain, scope) }
    private val erc721ContractIndexer: ERC721ContractIndexer = getKoin().get { parametersOf(chain, scope) }

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

        for (removed in current.subtract(updated)) {
            addressIndexers.remove(removed)
        }
        for (inserted in updated.subtract(current)) {
            addressIndexers[inserted] = setOf(
                getKoin().get<AccountTransactionIndexer> { parametersOf(chain, scope, inserted) },
                getKoin().get<BalanceIndexer> { parametersOf(chain, scope, inserted) }
            )
        }
    }
}
