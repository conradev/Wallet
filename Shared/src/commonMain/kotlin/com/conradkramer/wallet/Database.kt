package com.conradkramer.wallet

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.db.SqlDriver
import com.conradkramer.wallet.bigint.BigInteger
import com.conradkramer.wallet.browser.BrowserPermissionStore
import com.conradkramer.wallet.browser.prompt.Prompt
import com.conradkramer.wallet.crypto.PublicKey
import com.conradkramer.wallet.data.Account
import com.conradkramer.wallet.data.Browser_permission
import com.conradkramer.wallet.data.Browser_prompt
import com.conradkramer.wallet.data.Cb_crypto_currency
import com.conradkramer.wallet.data.Cb_exchange_rate
import com.conradkramer.wallet.data.Erc20_balance
import com.conradkramer.wallet.data.Erc20_contract
import com.conradkramer.wallet.data.Erc721_contract
import com.conradkramer.wallet.data.Eth_account_transaction
import com.conradkramer.wallet.data.Eth_balance
import com.conradkramer.wallet.data.Eth_block
import com.conradkramer.wallet.data.Eth_log
import com.conradkramer.wallet.data.Eth_receipt
import com.conradkramer.wallet.data.Eth_transaction
import com.conradkramer.wallet.data.Public_key
import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.ethereum.types.Quantity
import com.conradkramer.wallet.sql.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Instant

typealias PublicKeyRecord = Public_key
internal typealias AccountRecord = Account

val Database.Companion.FILE_NAME: String
    get() = "Wallet.db"

private val stateAdapter = object : ColumnAdapter<BrowserPermissionStore.State, Long> {
    override fun decode(databaseValue: Long) =
        BrowserPermissionStore.State(databaseValue) ?: BrowserPermissionStore.State.UNSPECIFIED
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

private val codeAdapter = object : ColumnAdapter<Currency.Code, String> {
    override fun decode(databaseValue: String) = Currency.Code(databaseValue)
    override fun encode(value: Currency.Code) = value.code
}

private val publicKeyAdapter = object : ColumnAdapter<PublicKey, ByteArray> {
    override fun decode(databaseValue: ByteArray) = PublicKey(databaseValue)
    override fun encode(value: PublicKey) = value.encoded(true)
}

private val dataAdapter = object : ColumnAdapter<Data, ByteArray> {
    override fun decode(databaseValue: ByteArray) = Data(databaseValue)

    // https://github.com/touchlab/SQLiter/pull/92
    override fun encode(value: Data) = if (value.data.isEmpty()) byteArrayOf(0) else value.data
}

private val addressAdapter = object : ColumnAdapter<Address, ByteArray> {
    override fun decode(databaseValue: ByteArray): Address = Address(databaseValue)
    override fun encode(value: Address): ByteArray = value.data
}

private val quantityAdapter = object : ColumnAdapter<Quantity, ByteArray> {
    override fun decode(databaseValue: ByteArray) = Quantity(databaseValue)

    // https://github.com/touchlab/SQLiter/pull/92
    override fun encode(value: Quantity) = if (value.data.isEmpty()) byteArrayOf(0) else value.data
}

private val quantityLongAdapter = object : ColumnAdapter<Quantity, Long> {
    override fun decode(databaseValue: Long) = Quantity(BigInteger.valueOf(databaseValue))
    override fun encode(value: Quantity) = value.toLong()
}

private val timestampAdapter = object : ColumnAdapter<Instant, String> {
    override fun decode(databaseValue: String): Instant = Instant.parse(databaseValue)
    override fun encode(value: Instant): String = value.toString()
}

private val chainAdapter = object : ColumnAdapter<Chain, Long> {
    override fun decode(databaseValue: Long) = Chain(databaseValue)
    override fun encode(value: Chain) = value.id
}

internal fun Database.Companion.invoke(driver: SqlDriver): Database {
    return Database(
        driver,
        browser_permissionAdapter = Browser_permission.Adapter(
            stateAdapter = stateAdapter,
        ),
        browser_promptAdapter = Browser_prompt.Adapter(
            promptAdapter = promptAdapter,
        ),
        cb_crypto_currencyAdapter = Cb_crypto_currency.Adapter(
            updated_atAdapter = timestampAdapter,
            codeAdapter = codeAdapter,
        ),
        cb_exchange_rateAdapter = Cb_exchange_rate.Adapter(
            fromAdapter = codeAdapter,
            toAdapter = codeAdapter,
            updated_atAdapter = timestampAdapter,
        ),
        erc20_balanceAdapter = Erc20_balance.Adapter(
            chain_idAdapter = chainAdapter,
            contractAdapter = addressAdapter,
            addressAdapter = addressAdapter,
            balanceAdapter = quantityAdapter,
        ),
        erc20_contractAdapter = Erc20_contract.Adapter(
            chain_idAdapter = chainAdapter,
            addressAdapter = addressAdapter,
            total_supplyAdapter = quantityAdapter,
            symbolAdapter = codeAdapter,
        ),
        erc721_contractAdapter = Erc721_contract.Adapter(
            chain_idAdapter = chainAdapter,
            addressAdapter = addressAdapter,
            symbolAdapter = codeAdapter,
            total_supplyAdapter = quantityAdapter,
        ),
        eth_account_transactionAdapter = Eth_account_transaction.Adapter(
            chain_idAdapter = chainAdapter,
            addressAdapter = addressAdapter,
            blockAdapter = quantityLongAdapter,
            hashAdapter = dataAdapter,
        ),
        eth_balanceAdapter = Eth_balance.Adapter(
            chain_idAdapter = chainAdapter,
            addressAdapter = addressAdapter,
            balanceAdapter = quantityAdapter,
        ),
        eth_blockAdapter = Eth_block.Adapter(
            chain_idAdapter = chainAdapter,
            timestampAdapter = timestampAdapter,
        ),
        eth_logAdapter = Eth_log.Adapter(
            chain_idAdapter = chainAdapter,
            tx_hashAdapter = dataAdapter,
            addressAdapter = addressAdapter,
            topic_0Adapter = dataAdapter,
            topic_1Adapter = dataAdapter,
            topic_2Adapter = dataAdapter,
            topic_3Adapter = dataAdapter,
            data_Adapter = dataAdapter,
        ),
        eth_receiptAdapter = Eth_receipt.Adapter(
            chain_idAdapter = chainAdapter,
            tx_hashAdapter = dataAdapter,
            contract_addressAdapter = addressAdapter,
        ),
        eth_transactionAdapter = Eth_transaction.Adapter(
            chain_idAdapter = chainAdapter,
            blockAdapter = quantityLongAdapter,
            hashAdapter = dataAdapter,
            fromAdapter = addressAdapter,
            toAdapter = addressAdapter,
            value_Adapter = quantityAdapter,
            data_Adapter = dataAdapter,
        ),
        public_keyAdapter = Public_key.Adapter(
            coinAdapter = coinAdapter,
            encodedAdapter = publicKeyAdapter,
        ),
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
