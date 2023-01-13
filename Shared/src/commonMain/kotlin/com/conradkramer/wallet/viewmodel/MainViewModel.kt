package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.Account
import com.conradkramer.wallet.AccountStore
import com.conradkramer.wallet.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Factory
import kotlin.coroutines.EmptyCoroutineContext

@Factory
class MainViewModel internal constructor(private val accountStore: AccountStore) {
    enum class Tab {
        BALANCE,
        COLLECTIBLES,
        TRANSFER,
        UTILITY,
        TRANSACTIONS;

        val title: String
            get() = when (this) {
                BALANCE -> "Balance"
                COLLECTIBLES -> "NFTs"
                TRANSFER -> "Send"
                UTILITY -> "Browser"
                TRANSACTIONS -> "Account"
            }
    }

    private val scope = CoroutineScope(EmptyCoroutineContext)

    val selectedTab = MutableStateFlow(Tab.BALANCE)

    val showOnboarding: StateFlow<Boolean>
        get() = accountStore.accounts.mapState(scope, List<Account>::isEmpty)
}
