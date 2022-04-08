package com.conradkramer.wallet

import com.conradkramer.wallet.ethereum.AlchemyProvider
import com.conradkramer.wallet.ethereum.InfuraProvider
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.RpcProvider
import com.conradkramer.wallet.sql.Database
import com.conradkramer.wallet.viewmodel.BalanceViewModel
import com.conradkramer.wallet.viewmodel.ImportViewModel
import com.conradkramer.wallet.viewmodel.MainViewModel
import com.conradkramer.wallet.viewmodel.OnboardingViewModel
import com.conradkramer.wallet.viewmodel.WelcomeViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.binds
import org.koin.dsl.module

internal val sharedModule = module {
    singleOf(::KeyStore)
    single<AccountStore> { DatabaseAccountStore(get(), get()) }
    singleOf(::EthereumBalanceStore) binds arrayOf(BalanceStore::class)

    // View Models
    factoryOf(::ImportViewModel)
    factoryOf(::MainViewModel)
    factoryOf(::OnboardingViewModel)
    factoryOf(::WelcomeViewModel)
    factoryOf(::BalanceViewModel)

    single { Database.invoke(get()) }
    single<RpcProvider> { AlchemyProvider("tbOMWQYmtAGuUDnDOhoJFYxXIKctXij3") }
    factory { RpcClient(get<RpcProvider>().endpointUrl) }
    factory { EthereumBalanceUpdater(get(), get()) } binds arrayOf(BalanceUpdater::class)
}

internal val infuraModule = module {
    single<RpcProvider> { InfuraProvider("ef01c7a0107b41deb6f77b00bda654b1") }
}
