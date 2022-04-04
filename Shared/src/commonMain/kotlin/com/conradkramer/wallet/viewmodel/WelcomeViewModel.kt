package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.AccountStore
import kotlinx.coroutines.flow.MutableStateFlow

class WelcomeViewModel internal constructor(private val accountStore: AccountStore) {
    enum class Option {
        IMPORT_PHRASE,
        GENERATE
    }

    val selectedOption = MutableStateFlow(Option.IMPORT_PHRASE)

    val options: List<Option>
        get() = if (accountStore.canStore) {
            Option.values().toList()
        } else {
            listOf()
        }

    val title = "Getting Started"
    val subtitle = "In order to start using Wallet, you'll need to import or create an account:"
}

val WelcomeViewModel.Option.title: String
    get() = when (this) {
        WelcomeViewModel.Option.IMPORT_PHRASE -> "Import Recovery Phrase"
        WelcomeViewModel.Option.GENERATE -> "Create New"
    }
