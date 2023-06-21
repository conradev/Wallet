package com.conradkramer.wallet

import com.conradkramer.wallet.browser.BrowserPermissionStore
import com.conradkramer.wallet.clients.CoinbaseClient
import com.conradkramer.wallet.ethereum.AlchemyProvider
import com.conradkramer.wallet.ethereum.Cloudflare
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.RpcProvider
import com.conradkramer.wallet.ethereum.indexing.AccountTransactionIndexer
import com.conradkramer.wallet.ethereum.indexing.BalanceIndexer
import com.conradkramer.wallet.ethereum.indexing.ChainIndexer
import com.conradkramer.wallet.ethereum.indexing.ERC20ContractIndexer
import com.conradkramer.wallet.ethereum.indexing.ERC721ContractIndexer
import com.conradkramer.wallet.ethereum.indexing.ReceiptIndexer
import com.conradkramer.wallet.ethereum.indexing.TransactionIndexer
import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.indexing.AppIndexer
import com.conradkramer.wallet.indexing.CoinbaseIndexer
import com.conradkramer.wallet.sql.Database
import com.conradkramer.wallet.viewmodel.PermissionPromptViewModel
import com.conradkramer.wallet.viewmodel.SignDataPromptViewModel
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.Koin
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
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
import org.koin.ksp.generated.module

@Module @ComponentScan
internal class AnnotationModule

private fun sharedModule() = module {
    includes(AnnotationModule().module)

    single { DatabaseAccountStore(get(), get(), logger<AccountStore>()) } bind AccountStore::class

    // View Models
    factory { params -> PermissionPromptViewModel(params.get()) }
    factory { params -> SignDataPromptViewModel(params.get(), get(), params.getOrNull(), get()) }

    single { Database.invoke(get()) }
    single { AlchemyProvider(mapOf(Chain.MAINNET to "tbOMWQYmtAGuUDnDOhoJFYxXIKctXij3")) }
    single { Cloudflare("eth.soup.solutions") } bind RpcProvider::class
    singleOf(::AppIndexer)

    // TODO: Add parameter for `chain`
    factory { RpcClient(get<RpcProvider>().endpointUrl(Chain.MAINNET), logger<RpcClient>()) }

    factory { CoinbaseClient(logger<CoinbaseClient>()) }
    factory { BrowserPermissionStore(get(), logger<BrowserPermissionStore>()) }
    factory { params -> CoinbaseIndexer(params[0], params[1], get(), get(), logger<CoinbaseIndexer>()) }
    factory { params -> ChainIndexer(params[0], params[1], get(), get()) }
    factory { params -> TransactionIndexer(params[0], params[1], get(), get(), logger<TransactionIndexer>()) }
    factory { params -> ReceiptIndexer(params[0], params[1], get(), get(), logger<ReceiptIndexer>()) }
    factory { params -> ERC20ContractIndexer(params[0], params[1], get(), get(), logger<ERC20ContractIndexer>()) }
    factory { params -> ERC721ContractIndexer(params[0], params[1], get(), get(), logger<TransactionIndexer>()) }
    factory { params ->
        val client = RpcClient(get<AlchemyProvider>().endpointUrl(params[0]), logger<RpcClient>())
        AccountTransactionIndexer(params[0], params[1], get(), client, params[2], logger<TransactionIndexer>())
    }
    factory { params -> BalanceIndexer(params[0], params[1], get(), get(), params[2], logger<TransactionIndexer>()) }

    factory { params -> KotlinLogging.logger(if (params.isNotEmpty()) params.get() else "General") }
}

internal expect fun platformModule(): org.koin.core.module.Module

internal fun mockApplication() = koinApplication {
    modules(platformModule(), sharedModule(), mockModule())
}

internal fun startKoinShared(declaration: KoinAppDeclaration) = startKoin {
    declaration()
    logger(KLoggerLogger())
    allowOverride(false)
    modules(platformModule(), sharedModule())
}
    .also { LaunchOptions.current.apply(it.koin) }

internal fun Scope.logger(qualifier: Qualifier) = get<KLogger> {
    parametersOf(
        when (qualifier) {
            is TypeQualifier -> qualifier.type.simpleName ?: qualifier.value
            else -> qualifier.value
        }
    )
}

internal fun Koin.logger(qualifier: Qualifier) = get<KLogger> {
    parametersOf(
        when (qualifier) {
            is TypeQualifier -> qualifier.type.simpleName ?: qualifier.value
            else -> qualifier.value
        }
    )
}

internal inline fun <reified T> Scope.logger() = logger(named<T>())
internal inline fun <reified T> Koin.logger() = logger(named<T>())

private class KLoggerLogger(private val logger: KLogger) : Logger() {
    constructor(name: String) : this(KotlinLogging.logger(name))
    constructor() : this("Koin")

    override fun display(level: Level, msg: MESSAGE) {
        when (level) {
            Level.DEBUG -> logger.debug { msg }
            Level.INFO -> logger.info { msg }
            Level.WARNING -> logger.warn { msg }
            Level.ERROR -> logger.error { msg }
            Level.NONE -> logger.warn { msg }
        }
    }
}
