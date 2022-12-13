package com.conradkramer.wallet.browser

import com.conradkramer.wallet.Account
import com.conradkramer.wallet.sql.Database
import mu.KLogger

internal class BrowserPermissionStore(private val database: Database, private val logger: KLogger) {

    fun allow(account: Account, domain: String, permission: BrowserPermission) {
        database.browserPermissionQueries.allow(account.id, domain, permission)
        logger.info { "Granted $domain permission $permission" }
    }

    fun deny(account: Account, domain: String, permission: BrowserPermission) {
        database.browserPermissionQueries.deny(account.id, domain, permission)
        logger.info { "Denied $domain permission $permission" }
    }

    fun state(account: Account, domain: String, permission: BrowserPermission): BrowserPermission.State {
        logger.info { "Checking $permission for ${account.ethereumAddress} on \"$domain\"" }
        val state = database.browserPermissionQueries.state(account.id, domain, permission)
            .executeAsOneOrNull() ?: BrowserPermission.State.UNSPECIFIED
        logger.info { "$permission for ${account.ethereumAddress} on \"$domain\": $state" }
        return state
    }
}
