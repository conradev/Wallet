package com.conradkramer.wallet

import com.conradkramer.wallet.sql.Database
import com.squareup.sqldelight.runtime.coroutines.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mu.KLogger

internal interface AccountStore {
    val canStore: Boolean

    val accounts: Collection<Account>
    val accountsFlow: Flow<Collection<Account>>

    fun add(mnemonic: Mnemonic): Account
}

internal class DatabaseAccountStore(
    private val database: Database,
    private val keyStore: KeyStore,
    private val logger: KLogger
) : AccountStore {

    override val canStore: Boolean
        get() = keyStore.canStore

    override val accounts: Collection<Account>
        get() = database
            .publicKeyQueries
            .keysForSeed(keyStore.all)
            .executeAsList()
            .accounts

    override val accountsFlow: Flow<Collection<Account>>
        get() = database
            .publicKeyQueries
            .keysForSeed(keyStore.all)
            .asFlow()
            .map { this.accounts } // TODO: Fix this to work with `it.accounts`

    override fun add(mnemonic: Mnemonic): Account {
        logger.info { "Adding account for mnemonic of length ${mnemonic.length}" }
        val seed = mnemonic.seed()

        val id = keyStore.add(seed)
        logger.info { "Inserted seed into key store for $id" }

        val root = ExtendedPrivateKey.fromSeed(seed)
        val keys = Coin.values()
            .map {
                PublicKeyRecord(
                    id,
                    it,
                    0,
                    false,
                    0,
                    root.address(it, account = 0, address = 0)
                        .publicKey.key
                )
            }

        database.transaction {
            keys.forEach { database.publicKeyQueries.insert(it) }
        }
        logger.info { "Inserted ${keys.size} public keys for $id" }

        return Account(id, keys)
    }
}

internal val List<PublicKeyRecord>.accounts: Collection<Account>
    get() = groupBy { it.seed_id }
        .mapValues { Account(it.key, it.value) }
        .values
