package com.conradkramer.wallet

import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.Quantity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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

internal class MockBalanceStore() : BalanceStore {
    override fun getBalances(address: Address) : Flow<Collection<Balance>> {
        return flowOf()
    }

    override fun add(balance: Balance) : Balance = Balance(
        Address("foo".encodeToByteArray()),
        null,
        Quantity((10).toUInt().toByteArray())
    )
}

internal class MockBalanceUpdater() : BalanceUpdater {
    override fun update(address: Address) = Unit
}

class PreviewMocks {
    companion object {
        private val mockModule = module {
            single<AccountStore> { MockAccountStore() }
            single<BalanceStore> { MockBalanceStore() }
            single<BalanceUpdater> { MockBalanceUpdater() }
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
