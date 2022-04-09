@file:JvmName("KoinAndroid")
package com.conradkramer.wallet

import android.content.Context
import com.conradkramer.wallet.sql.Database
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal fun androidModule() = module {
    includes(sharedModule())
    factory<SqlDriver> { AndroidSqliteDriver(Database.Schema, get(), Database.FILE_NAME) }
}

fun startKoin(context: Context): KoinApplication {
    return startKoin {
        logger(KLoggerLogger())
        androidContext(context)
        allowOverride(false)
        modules(androidModule())
    }
}
