package com.conradkramer.wallet

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import org.koin.dsl.module

internal class MockAccountStore : AccountStore {
    override val canStore = true
    override val accounts: Collection<Account> = listOf()
    override val accountsFlow: Flow<Collection<Account>> = MutableStateFlow(accounts)

    override fun add(mnemonic: Mnemonic): Account {
        return Account("mock")
    }
}

class PreviewMocks {
    companion object {
        private val mockModule = module {
            single<AccountStore> { MockAccountStore() }
        }

        private val application = koinApplication {
            modules(sharedModule, mockModule)
        }

        val koin: Koin
            get() = application.koin

        inline fun <reified T> get(): T {
            return koin.get()
        }
    }
}
