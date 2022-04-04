@file:JvmName("KoinAndroid")
package com.conradkramer.wallet

import android.content.Context
import com.conradkramer.wallet.sql.Database
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val androidModule = module {
    includes(sharedModule)
    factory<SqlDriver> { AndroidSqliteDriver(Database.Schema, get(), Database.FILE_NAME) }
    factoryOf(::KeyStoreContext)
}

fun startKoin(context: Context): KoinApplication {
    return startKoin {
        androidLogger()
        androidContext(context)
        allowOverride(false)
        modules(androidModule)
    }
}
