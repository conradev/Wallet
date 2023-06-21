package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.Account
import com.conradkramer.wallet.AccountStore
import com.conradkramer.wallet.Balance
import com.conradkramer.wallet.Currency
import com.conradkramer.wallet.Locale
import com.conradkramer.wallet.NumberFormatter
import com.conradkramer.wallet.bigint.BigInteger
import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.mapState
import com.conradkramer.wallet.sql.Database
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Factory
import kotlin.coroutines.EmptyCoroutineContext

abstract class BalancesViewModel {
    @NativeCoroutinesState
    abstract val accountName: StateFlow<String>

    @NativeCoroutinesState
    abstract val totalBalance: StateFlow<String>

    @NativeCoroutinesState
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

    private val fiat: Currency.Code
        get() = Locale.current.currencyCode ?: Currency.Code.USD

    override val assets: StateFlow<List<Asset>> = account.value
        ?.let { accessor.holdings(scope, chain.value, it.ethereumAddress, fiat) }
        ?: MutableStateFlow(listOf())

    override val totalBalance = assets.mapState(scope) { assets ->
        val sum = assets.map(Asset::fiat).reduceOrNull(Balance::plus)
            ?: Balance(Currency.USD, BigInteger.valueOf(0))
        NumberFormatter.fiat(sum.currency).string(sum.toDouble())
    }
}
