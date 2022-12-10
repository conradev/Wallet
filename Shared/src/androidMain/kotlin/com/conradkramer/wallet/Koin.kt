@file:JvmName("KoinAndroid")

package com.conradkramer.wallet

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.binds
import org.koin.dsl.module

internal actual fun platformModule() = module {
    factoryOf(::HardwareKeyStore) binds arrayOf(BiometricAuthenticator::class, KeyStore::class)
}

fun startKoinWithDriver(context: Context, driver: () -> SqlDriver): KoinApplication {
    return startKoinShared {
        modules(module { factory { driver() } })
        androidContext(context)
    }
}
