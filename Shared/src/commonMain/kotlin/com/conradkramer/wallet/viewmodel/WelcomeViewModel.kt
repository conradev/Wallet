package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.AccountStore
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.annotation.Factory

@Factory
class WelcomeViewModel internal constructor(private val accountStore: AccountStore) {
    enum class Option {
        IMPORT_PHRASE,
        GENERATE;

        val title: String
            get() = when (this) {
                IMPORT_PHRASE -> "Import Recovery Phrase"
                GENERATE -> "Create New"
            }
    }

    @NativeCoroutinesState
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
