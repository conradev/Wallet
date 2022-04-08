package com.conradkramer.wallet

import com.conradkramer.wallet.data.Ethereum_balance
import com.conradkramer.wallet.data.Public_key
import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.Quantity
import com.conradkramer.wallet.platform.PublicKey
import com.conradkramer.wallet.sql.Database
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.datetime.LocalDateTime

typealias PublicKeyRecord = Public_key

internal val Database.Companion.FILE_NAME: String
    get() = "Wallet.db"

enum class Coin(val number: Int) {
    BITCOIN(0),
    ETHEREUM(60);
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

private val addressAdapter = object : ColumnAdapter<Address, String> {
    override fun decode(databaseValue: String): Address = Address.fromString(databaseValue)
    override fun encode(value: Address): String = value.toString()
}

private val quantityAdapter = object : ColumnAdapter<Quantity, ByteArray> {
    override fun decode(databaseValue: ByteArray) = Quantity(databaseValue)
    override fun encode(value: Quantity) = value.data
}

private val dateAdapter = object : ColumnAdapter<LocalDateTime, String> {
    override fun encode(value: LocalDateTime): String = value.toString()
    override fun decode(databaseValue: String): LocalDateTime = databaseValue.trim().replace(" ", "T")
        .let { LocalDateTime.parse(it) }
}

internal fun Database.Companion.invoke(driver: SqlDriver): Database {
    return Database(
        driver,
        public_keyAdapter = Public_key.Adapter(
            coinAdapter = coinAdapter,
            encodedAdapter = encodedAdapter
        ),
        ethereum_balanceAdapter = Ethereum_balance.Adapter(
            addressAdapter = addressAdapter,
            balanceAdapter = quantityAdapter,
            contract_addressAdapter = addressAdapter,
            timestampAdapter = dateAdapter
        ),
    )
}
