package com.conradkramer.wallet

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.db.SqlDriver
import com.conradkramer.wallet.browser.BrowserPermissionStore
import com.conradkramer.wallet.browser.prompt.Prompt
import com.conradkramer.wallet.crypto.PublicKey
import com.conradkramer.wallet.data.Browser_permission
import com.conradkramer.wallet.data.Browser_prompt
import com.conradkramer.wallet.data.Public_key
import com.conradkramer.wallet.sql.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

typealias PublicKeyRecord = Public_key
typealias AccountRecord = com.conradkramer.wallet.data.Account

val Database.Companion.FILE_NAME: String
    get() = "Wallet.db"

private val stateAdapter = object : ColumnAdapter<BrowserPermissionStore.State, Long> {
    override fun decode(databaseValue: Long) = BrowserPermissionStore.State.values()
        .firstOrNull { it.value == databaseValue } ?: BrowserPermissionStore.State.UNSPECIFIED
    override fun encode(value: BrowserPermissionStore.State) = value.value
}

private val promptAdapter = object : ColumnAdapter<Prompt, String> {
    override fun decode(databaseValue: String) = Prompt.decodeFromString<Prompt>(databaseValue)
    override fun encode(value: Prompt) = value.encodeToString()
}

private val coinAdapter = object : ColumnAdapter<Coin, Long> {
    override fun decode(databaseValue: Long) =
        Coin.values().first { it.number.toLong() == databaseValue }
    override fun encode(value: Coin) = value.number.toLong()
}

private val encodedAdapter = object : ColumnAdapter<PublicKey, ByteArray> {
    override fun decode(databaseValue: ByteArray) = PublicKey(databaseValue)
    override fun encode(value: PublicKey) = value.encoded(true)
}

internal fun Database.Companion.invoke(driver: SqlDriver): Database {
    return Database(
        driver,
        browser_permissionAdapter = Browser_permission.Adapter(
            stateAdapter = stateAdapter
        ),
        browser_promptAdapter = Browser_prompt.Adapter(
            promptAdapter = promptAdapter
        ),
        public_keyAdapter = Public_key.Adapter(
            coinAdapter = coinAdapter,
            encodedAdapter = encodedAdapter
        )
    )
}

internal fun <T : Any, R> Query<T>.asStateFlow(scope: CoroutineScope, content: (Query<T>) -> R): StateFlow<R> {
    return asFlow()
        .map(content)
        .stateIn(scope, SharingStarted.WhileSubscribed(), content(this))
}

internal fun <T, R> StateFlow<T>.mapState(scope: CoroutineScope, content: (T) -> R): StateFlow<R> {
    return map(content)
        .stateIn(scope, SharingStarted.WhileSubscribed(), content(this.value))
}
