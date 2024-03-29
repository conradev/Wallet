package com.conradkramer.wallet

import com.conradkramer.wallet.browser.message.Frame
import com.conradkramer.wallet.browser.message.Session
import com.conradkramer.wallet.browser.prompt.PermissionPrompt
import com.conradkramer.wallet.browser.prompt.SignDataPrompt
import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.ethereum.types.Quantity
import com.conradkramer.wallet.viewmodel.Asset
import com.conradkramer.wallet.viewmodel.BalancesViewModel
import com.conradkramer.wallet.viewmodel.PermissionPromptViewModel
import com.conradkramer.wallet.viewmodel.SignDataPromptViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

private fun Account.Companion.random(): Account {
    return Account(
        "id",
        0,
        ExtendedPrivateKey
            .fromSeed(Mnemonic().seed())
            .publicKeys("id", 0),
    )
}

private class MockAccountStore(val account: Account) : AccountStore {
    override val canStore = true
    override val accounts: StateFlow<List<Account>> = MutableStateFlow(listOf(account))

    override fun add(mnemonic: Mnemonic): Account {
        return account
    }

    override fun delete(account: Account) {
    }

    override fun reset() {
    }

    override fun context(account: Account): AuthenticationContext {
        return AuthenticationContext(account.id)
    }

    override suspend fun <R> authenticate(
        context: AuthenticationContext,
        info: BiometricPromptInfo,
        host: BiometricPromptHost?,
        handler: (root: ExtendedPrivateKey?) -> R,
    ): R {
        return handler(null)
    }
}

internal class MockBalancesViewModel : BalancesViewModel() {
    override val accountName = MutableStateFlow("conradkramer.eth")
    override val totalBalance = MutableStateFlow("$5,623.20")
    override val assets = MutableStateFlow(
        listOf(
            Asset(
                Balance(Currency.ETH, Quantity.fromString("0x14f952c14b42909e").value),
                Currency.USD,
                0.000616750955964,
            ),
        ),
    )
}

internal fun mockModule() = module {
    val account = Account.random()

    single { MockAccountStore(account) } bind AccountStore::class
    factory { PermissionPrompt(Frame.zero, Session(0, 0, 0), account.id, "app.ens.domains") }
    factory {
        SignDataPrompt(
            "1234",
            Frame.zero,
            Session(0, 0, 0),
            "mint.fun",
            account.ethereumAddress,
            Data.fromString("0x9b2055d370f73ec7d8a03e965129118dc8f5bf83"),
        )
    }

    factoryOf(::PermissionPromptViewModel)
    factory { SignDataPromptViewModel(get(), get(), getOrNull(), get()) }

    singleOf(::MockBalancesViewModel) bind BalancesViewModel::class
}

class PreviewMocks {
    companion object {
        private val application: KoinApplication by lazy { mockApplication() }

        val koin: Koin
            get() = application.koin

        inline fun <reified T> get(): T = koin.get()
    }
}
