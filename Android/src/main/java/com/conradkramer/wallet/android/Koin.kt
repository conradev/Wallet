package com.conradkramer.wallet.android

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.conradkramer.wallet.FILE_NAME
import com.conradkramer.wallet.sql.Database
import com.conradkramer.wallet.startKoinWithDriver
import org.dbtools.android.room.sqliteorg.SqliteOrgSQLiteOpenHelperFactory

fun startKoin(context: Context) {
    startKoinWithDriver(context) {
        AndroidSqliteDriver(Database.Schema, context, Database.FILE_NAME, SqliteOrgSQLiteOpenHelperFactory())
    }
}
