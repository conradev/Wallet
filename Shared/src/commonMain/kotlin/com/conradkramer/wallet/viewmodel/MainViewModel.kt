package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.Account
import com.conradkramer.wallet.AccountStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

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
                UTILITY -> "Utilities"
                TRANSACTIONS -> "Account"
            }
    }

    val selectedTab = MutableStateFlow(Tab.BALANCE)

    val showOnboarding: Boolean
        get() = accountStore.accounts.isEmpty()

    val showOnboardingFlow: Flow<Boolean>
        get() = accountStore.accountsFlow.map(Collection<Account>::isEmpty)
}
