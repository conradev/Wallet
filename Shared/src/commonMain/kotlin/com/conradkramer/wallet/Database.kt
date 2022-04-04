package com.conradkramer.wallet

import com.conradkramer.wallet.data.Public_key
import com.conradkramer.wallet.platform.PublicKey
import com.conradkramer.wallet.sql.Database
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlDriver

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

internal fun Database.Companion.invoke(driver: SqlDriver): Database {
    return Database(
        driver,
        public_keyAdapter = Public_key.Adapter(
            coinAdapter = coinAdapter,
            encodedAdapter = encodedAdapter
        )
    )
}
