package com.conradkramer.wallet

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.sqliter.DatabaseConfiguration
import com.conradkramer.wallet.browser.BrowserMessageHost
import com.conradkramer.wallet.browser.BrowserPromptHost
import com.conradkramer.wallet.indexing.AppIndexer
import com.conradkramer.wallet.sql.Database
import com.conradkramer.wallet.viewmodel.BalancesViewModel
import com.conradkramer.wallet.viewmodel.BrowserViewModel
import com.conradkramer.wallet.viewmodel.ImportViewModel
import com.conradkramer.wallet.viewmodel.MainViewModel
import com.conradkramer.wallet.viewmodel.OnboardingViewModel
import com.conradkramer.wallet.viewmodel.PermissionPromptViewModel
import com.conradkramer.wallet.viewmodel.SignDataPromptViewModel
import com.conradkramer.wallet.viewmodel.WelcomeViewModel
import io.github.oshai.kotlinlogging.KotlinLoggingConfiguration
import io.github.oshai.kotlinlogging.OSLogSubsystemAppender
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.dsl.binds
import org.koin.dsl.module
import platform.Foundation.NSFileManager

internal fun darwinModule() = module {
    single { HardwareKeyStore(getProperty("app_group_identifier"), logger<HardwareKeyStore>()) } binds arrayOf(
        BiometricAuthenticator::class,
        KeyStore::class
    )
    single<SqlDriver> {
        val applicationGroup: String = getProperty("app_group_identifier")
        val basePath = NSFileManager
            .defaultManager
            .containerURLForSecurityApplicationGroupIdentifier(applicationGroup)?.path
            ?: throw Exception("Unable to get path for application group container")

        val schema = Database.Schema
        val configuration = DatabaseConfiguration(
            name = Database.FILE_NAME,
            version = schema.version,
            extendedConfig = DatabaseConfiguration.Extended(
                foreignKeyConstraints = true,
                basePath = basePath
            ),
            create = { connection ->
                wrapConnection(connection) { schema.create(it) }
            },
            upgrade = { connection, oldVersion, newVersion ->
                wrapConnection(connection) { schema.migrate(it, oldVersion, newVersion) }
            }
        )
        NativeSqliteDriver(configuration)
    }
}

fun KoinApplication.Companion.start(
    applicationGroup: String,
    viewServiceName: String,
    subsystem: String
): KoinApplication {
    KotlinLoggingConfiguration.appender = OSLogSubsystemAppender(subsystem)
    CrashReporterClient.installHook()
    return startKoinShared {
        properties(
            mapOf(
                "app_group_identifier" to applicationGroup,
                "view_service_identifier" to viewServiceName
            )
        )
    }
}

val Koin.mainViewModel: MainViewModel
    get() = get()

val Koin.onboardingViewModel: OnboardingViewModel
    get() = get()

val Koin.welcomeViewModel: WelcomeViewModel
    get() = get()

val Koin.importViewModel: ImportViewModel
    get() = get()

val Koin.browserViewModel: BrowserViewModel
    get() = get()

val Koin.balancesViewModel: BalancesViewModel
    get() = get()

val Koin.permissionPromptViewModel: PermissionPromptViewModel
    get() = get()

val Koin.signDataPromptViewModel: SignDataPromptViewModel
    get() = get()

val Koin.browserMessageHost: BrowserMessageHost
    get() = get()

val Koin.browserPromptHost: BrowserPromptHost
    get() = get()

val Koin.appIndexer: AppIndexer
    get() = get()
