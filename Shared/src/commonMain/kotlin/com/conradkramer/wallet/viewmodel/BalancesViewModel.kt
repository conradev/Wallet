package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.Account
import com.conradkramer.wallet.AccountStore
import com.conradkramer.wallet.BalanceStore
import com.conradkramer.wallet.DatabaseBalanceStore
import com.conradkramer.wallet.ethereum.BalanceUpdater
import com.conradkramer.wallet.ethereum.CmcClient
import com.conradkramer.wallet.ethereum.RpcBalanceUpdater
import com.conradkramer.wallet.ethereum.TokenUpdater
import com.conradkramer.wallet.mapState
import com.conradkramer.wallet.random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.EmptyCoroutineContext

class BalancesViewModel internal constructor(
    accountStore: AccountStore,
    private val client: CmcClient,
    private val tokenUpdater: TokenUpdater
) : KoinComponent {
    private val account = accountStore.accounts.value.firstOrNull() ?: Account.random()
    private val scope = CoroutineScope(EmptyCoroutineContext)
    private val balanceStore: BalanceStore by lazy { getKoin().get<DatabaseBalanceStore> { parametersOf(account.ethereumAddress) } }
    private val balanceUpdater: BalanceUpdater by lazy { getKoin().get<RpcBalanceUpdater> { parametersOf(account.ethereumAddress) } }

    val accountName: String = account.ethereumAddress.toString()
    val balances = balanceStore.balances
        .mapState(scope) { balances ->
            balances.map { BalanceViewModel(it.currency, it.quantity) }
        }

    val totalBalance: StateFlow<String>
        get() = MutableStateFlow("$0.00")

    init {
//        update()
    }

    private fun update() {
        balanceUpdater.update()
        tokenUpdater.update()
    }
}
