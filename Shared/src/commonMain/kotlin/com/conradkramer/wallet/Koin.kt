package com.conradkramer.wallet

import com.conradkramer.wallet.ethereum.AlchemyProvider
import com.conradkramer.wallet.ethereum.InfuraProvider
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.RpcProvider
import com.conradkramer.wallet.sql.Database
import com.conradkramer.wallet.viewmodel.ImportViewModel
import com.conradkramer.wallet.viewmodel.MainViewModel
import com.conradkramer.wallet.viewmodel.OnboardingViewModel
import com.conradkramer.wallet.viewmodel.WelcomeViewModel
import mu.KLogger
import mu.KotlinLogging
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.core.module.dsl.factoryOf
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.TypeQualifier
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun sharedModule() = module {
    includes(keyStoreModule())
    single<AccountStore> { DatabaseAccountStore(get(), get(), logger<AccountStore>()) }

    // View Models
    factoryOf(::ImportViewModel)
    factoryOf(::MainViewModel)
    factoryOf(::OnboardingViewModel)
    factoryOf(::WelcomeViewModel)

    single { Database.invoke(get()) }
    single { AlchemyProvider("tbOMWQYmtAGuUDnDOhoJFYxXIKctXij3") } bind RpcProvider::class
    single(named("infura")) { InfuraProvider("ef01c7a0107b41deb6f77b00bda654b1") } bind RpcProvider::class
    factory { RpcClient(get<RpcProvider>().endpointUrl) }

    factory { params -> KotlinLogging.logger(params.get<String>()) }
}

internal fun Scope.logger(qualifier: Qualifier) = get<KLogger> {
    parametersOf(
        when (qualifier) {
            is TypeQualifier -> qualifier.type.simpleName ?: qualifier.value
            else -> qualifier.value
        }
    )
}

internal inline fun <reified T> Scope.logger() = logger(named<T>())

internal class KLoggerLogger(private val logger: KLogger) : Logger() {
    constructor(name: String) : this(KotlinLogging.logger(name))
    constructor() : this("Koin")

    override fun log(level: Level, msg: MESSAGE) {
        when (level) {
            Level.DEBUG -> logger.debug { msg }
            Level.INFO -> logger.info { msg }
            Level.ERROR -> logger.error { msg }
            Level.NONE -> logger.warn { msg }
        }
    }
}
