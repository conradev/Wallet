package com.conradkramer.wallet

import com.conradkramer.wallet.browser.BrowserMessageHost
import com.conradkramer.wallet.browser.BrowserPermissionStore
import com.conradkramer.wallet.browser.BrowserPromptExecutor
import com.conradkramer.wallet.browser.BrowserPromptHost
import com.conradkramer.wallet.ethereum.AlchemyProvider
import com.conradkramer.wallet.ethereum.InfuraProvider
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.RpcProvider
import com.conradkramer.wallet.sql.Database
import com.conradkramer.wallet.viewmodel.BrowserViewModel
import com.conradkramer.wallet.viewmodel.ImportViewModel
import com.conradkramer.wallet.viewmodel.MainViewModel
import com.conradkramer.wallet.viewmodel.OnboardingViewModel
import com.conradkramer.wallet.viewmodel.PermissionPromptViewModel
import com.conradkramer.wallet.viewmodel.SignDataPromptViewModel
import com.conradkramer.wallet.viewmodel.WelcomeViewModel
import mu.KLogger
import mu.KotlinLogging
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.TypeQualifier
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.koinApplication
import org.koin.dsl.module

private fun sharedModule() = module {
    single { DatabaseAccountStore(get(), get(), logger<AccountStore>()) } bind AccountStore::class

    // View Models
    factoryOf(::ImportViewModel)
    factoryOf(::MainViewModel)
    factoryOf(::OnboardingViewModel)
    factoryOf(::WelcomeViewModel)
    factoryOf(::BrowserViewModel)
    factory { params -> PermissionPromptViewModel(params.get()) }
    factory { params -> SignDataPromptViewModel(params.get(), get(), params.getOrNull(), get()) }

    single { Database.invoke(get()) }
    single { AlchemyProvider("tbOMWQYmtAGuUDnDOhoJFYxXIKctXij3") } bind RpcProvider::class
    single(named("infura")) { InfuraProvider("ef01c7a0107b41deb6f77b00bda654b1") } bind RpcProvider::class
    factory { RpcClient(get<RpcProvider>().endpointUrl, logger<RpcClient>()) }

    factoryOf(::BrowserPromptExecutor)
    factoryOf(::BrowserPromptHost)
    singleOf(::BrowserMessageHost)
    factoryOf(::BrowserPermissionStore)

    factory { params -> KotlinLogging.logger(if (params.isNotEmpty()) params.get() else "General") }
}

internal expect fun platformModule(): Module

internal fun mockApplication(): KoinApplication {
    return koinApplication {
        modules(platformModule(), sharedModule(), mockModule())
    }
}

internal fun startKoinShared(declaration: KoinAppDeclaration): KoinApplication {
    val application = startKoin {
        declaration()
        logger(KLoggerLogger())
        allowOverride(false)
        modules(platformModule(), sharedModule())
    }

    if (LaunchOptions.current.reset) {
        application.koin.get<AccountStore>().reset()
    }

    return application
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

private class KLoggerLogger(private val logger: KLogger) : Logger() {
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
