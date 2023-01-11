package com.conradkramer.wallet

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.db.SqlDriver
import com.conradkramer.wallet.browser.BrowserPermissionStore
import com.conradkramer.wallet.browser.prompt.Prompt
import com.conradkramer.wallet.crypto.PublicKey
import com.conradkramer.wallet.data.Account
import com.conradkramer.wallet.data.Browser_permission
import com.conradkramer.wallet.data.Browser_prompt
import com.conradkramer.wallet.data.Erc20_contract
import com.conradkramer.wallet.data.Erc721_contract
import com.conradkramer.wallet.data.Eth_block
import com.conradkramer.wallet.data.Eth_token_transfer
import com.conradkramer.wallet.data.Eth_transaction
import com.conradkramer.wallet.data.Public_key
import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.Chain
import com.conradkramer.wallet.ethereum.Data
import com.conradkramer.wallet.ethereum.Quantity
import com.conradkramer.wallet.sql.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Instant

typealias PublicKeyRecord = Public_key
typealias AccountRecord = Account

val Database.Companion.FILE_NAME: String
    get() = "Wallet.db"

private val stateAdapter = object : ColumnAdapter<BrowserPermissionStore.State, Long> {
    override fun decode(databaseValue: Long) = BrowserPermissionStore.State(databaseValue) ?: BrowserPermissionStore.State.UNSPECIFIED
    override fun encode(value: BrowserPermissionStore.State) = value.value
}

private val promptAdapter = object : ColumnAdapter<Prompt, String> {
    override fun decode(databaseValue: String) = Prompt.decodeFromString<Prompt>(databaseValue)
    override fun encode(value: Prompt) = value.encodeToString()
}

private val coinAdapter = object : ColumnAdapter<Coin, Long> {
    override fun decode(databaseValue: Long) = Coin(databaseValue)
    override fun encode(value: Coin) = value.number
}

private val publicKeyAdapter = object : ColumnAdapter<PublicKey, ByteArray> {
    override fun decode(databaseValue: ByteArray) = PublicKey(databaseValue)
    override fun encode(value: PublicKey) = value.encoded(true)
}

private val dataAdapter = object : ColumnAdapter<Data, ByteArray> {
    override fun decode(databaseValue: ByteArray) = Data(databaseValue)
    override fun encode(value: Data) = value.data
}

private val addressAdapter = object : ColumnAdapter<Address, ByteArray> {
    override fun decode(databaseValue: ByteArray): Address = Address(databaseValue)
    override fun encode(value: Address): ByteArray = value.data
}

private val quantityAdapter = object : ColumnAdapter<Quantity, ByteArray> {
    override fun decode(databaseValue: ByteArray) = Quantity(databaseValue)

    // SQLiter seems to crash if we insert empty byte arrays
    override fun encode(value: Quantity) = if (value.data.isEmpty()) ByteArray(1) else value.data
}

private val timestampAdapter = object : ColumnAdapter<Instant, String> {
    override fun decode(databaseValue: String): Instant = Instant.parse(databaseValue)
    override fun encode(value: Instant): String = value.toString()
}

private val chainAdapter = object : ColumnAdapter<Chain, Long> {
    override fun decode(databaseValue: Long) = Chain(databaseValue)
    override fun encode(value: Chain) = value.id
}

internal fun Database.Companion.withAdapters(driver: SqlDriver): Database {
    return Database(
        driver,
        browser_permissionAdapter = Browser_permission.Adapter(
            stateAdapter = stateAdapter
        ),
        browser_promptAdapter = Browser_prompt.Adapter(
            promptAdapter = promptAdapter
        ),
        erc20_contractAdapter = Erc20_contract.Adapter(
            chain_idAdapter = chainAdapter,
            addressAdapter = addressAdapter,
            total_supplyAdapter = quantityAdapter
        ),
        erc721_contractAdapter = Erc721_contract.Adapter(
            chain_idAdapter = chainAdapter,
            addressAdapter = addressAdapter,
            total_supplyAdapter = quantityAdapter
        ),
        eth_blockAdapter = Eth_block.Adapter(
            chain_idAdapter = chainAdapter,
            timestampAdapter = timestampAdapter
        ),
        eth_transactionAdapter = Eth_transaction.Adapter(
            chain_idAdapter = chainAdapter,
            hashAdapter = dataAdapter,
            fromAdapter = addressAdapter,
            toAdapter = addressAdapter,
            value_Adapter = quantityAdapter
        ),
        eth_token_transferAdapter = Eth_token_transfer.Adapter(
            chain_idAdapter = chainAdapter,
            contractAdapter = addressAdapter,
            fromAdapter = addressAdapter,
            toAdapter = addressAdapter,
            value_Adapter = quantityAdapter
        ),
        public_keyAdapter = Public_key.Adapter(
            coinAdapter = coinAdapter,
            encodedAdapter = publicKeyAdapter
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
