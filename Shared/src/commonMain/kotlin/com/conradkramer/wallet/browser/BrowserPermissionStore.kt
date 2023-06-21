package com.conradkramer.wallet.browser

import com.conradkramer.wallet.Account
import com.conradkramer.wallet.sql.Database
import io.github.oshai.kotlinlogging.KLogger

class BrowserPermissionStore internal constructor(
    private val database: Database,
    private val logger: KLogger,
) {
    enum class State(val value: Long) {
        ALLOWED(1),
        DENIED(-1),
        UNSPECIFIED(0),
        ;

        companion object {
            private val mapping = State.values().associate { it.value to it }

            operator fun invoke(value: Long) = mapping[value]
        }
    }

    internal fun allow(account: Account, domain: String) {
        database.browserPermissionQueries.allow(account.id, domain)
        logger.info { "Granted \"$domain\" permission for ${account.ethereumAddress}" }
    }

    internal fun deny(account: Account, domain: String) {
        database.browserPermissionQueries.deny(account.id, domain)
        logger.info { "Denied \"$domain\" permission for ${account.ethereumAddress}" }
    }

    internal fun state(account: Account, domain: String): State {
        val state = database.browserPermissionQueries.state(account.id, domain)
            .executeAsOneOrNull() ?: State.UNSPECIFIED
        logger.info { "${account.ethereumAddress} is $state on \"$domain\"" }
        return state
    }
}
