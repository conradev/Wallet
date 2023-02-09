package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.Account
import com.conradkramer.wallet.AccountStore
import com.conradkramer.wallet.Currency
import com.conradkramer.wallet.Locale
import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.sql.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Factory
import kotlin.coroutines.EmptyCoroutineContext

abstract class BalancesViewModel {
    abstract val accountName: StateFlow<String>
    abstract val totalBalance: StateFlow<String>
    abstract val assets: StateFlow<List<Asset>>
}

@Factory(binds = [BalancesViewModel::class])
class DatabaseBalancesViewModel internal constructor(
    accountStore: AccountStore,
    private val database: Database
) : BalancesViewModel() {
    private val scope = CoroutineScope(EmptyCoroutineContext)

    private val chain: MutableStateFlow<Chain> = MutableStateFlow(Chain.MAINNET)
    private val account: MutableStateFlow<Account?> = MutableStateFlow(accountStore.accounts.value.firstOrNull())
    private val accessor = BalanceAccessor(database)

    override val accountName = MutableStateFlow("0x8a6752a88417e8f7d822dacaeb52ed8e6e591c43")
    override val totalBalance = MutableStateFlow("$1,000.00")

    private val fiat = Locale.current.currencyCode

    override val assets: StateFlow<List<Asset>> = account.value
        ?.let { accessor.holdings(scope, chain.value, it.ethereumAddress, fiat ?: Currency.Code.USD) }
        ?: MutableStateFlow(listOf())
}
