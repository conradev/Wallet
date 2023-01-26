package com.conradkramer.wallet

import com.conradkramer.wallet.sql.Database
import org.koin.core.Koin

internal data class LaunchOptions(
    val resetAccounts: Boolean,
    val resetIndex: Boolean
) {
    companion object {
        val default = LaunchOptions(
            resetAccounts = false,
            resetIndex = false
        )
        val current = launchOptionsForCurrentProcess()
    }

    fun apply(koin: Koin) {
        if (resetAccounts) {
            koin.get<AccountStore>().reset()
        }

        if (resetIndex) {
            val database = koin.get<Database>()
            database.transaction {
                database.coinbaseQueries.reset()
                database.ethereumQueries.reset()
            }
        }
    }
}

internal expect fun launchOptionsForCurrentProcess(): LaunchOptions
