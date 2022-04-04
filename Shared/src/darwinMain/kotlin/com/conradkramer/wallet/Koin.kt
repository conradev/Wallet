package com.conradkramer.wallet

import co.touchlab.sqliter.DatabaseConfiguration
import com.conradkramer.wallet.sql.Database
import com.conradkramer.wallet.viewmodel.ImportViewModel
import com.conradkramer.wallet.viewmodel.MainViewModel
import com.conradkramer.wallet.viewmodel.OnboardingViewModel
import com.conradkramer.wallet.viewmodel.WelcomeViewModel
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import com.squareup.sqldelight.drivers.native.wrapConnection
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import platform.Foundation.NSFileManager

data class ApplicationGroup(val value: String)

private val databaseModule = module {
    factory<SqlDriver> {
        val applicationGroup: ApplicationGroup = get()
        val basePath = NSFileManager.defaultManager.containerURLForSecurityApplicationGroupIdentifier(applicationGroup.value)?.path
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

val iosModule = module {
    includes(sharedModule, databaseModule)
    singleOf(::KeyStoreContext)
}

fun KoinApplication.Companion.start(applicationGroup: String, subsystem: String): KoinApplication {
    val injected = module {
        factory { ApplicationGroup(applicationGroup) }
    }
    return startKoin {
        logger(OSLogLogger(subsystem))
        allowOverride(false)
        modules(iosModule, injected)
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
