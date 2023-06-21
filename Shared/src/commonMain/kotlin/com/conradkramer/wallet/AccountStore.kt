package com.conradkramer.wallet

import app.cash.sqldelight.Query
import com.conradkramer.wallet.crypto.SHA256Digest
import com.conradkramer.wallet.encoding.encodeHex
import com.conradkramer.wallet.sql.Database
import io.github.oshai.kotlinlogging.KLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.EmptyCoroutineContext

internal interface AccountStore {
    val canStore: Boolean
    val accounts: StateFlow<List<Account>>

    fun add(mnemonic: Mnemonic): Account
    fun delete(account: Account)
    fun reset()

    fun context(account: Account): AuthenticationContext
    suspend fun <R> authenticate(
        context: AuthenticationContext,
        info: BiometricPromptInfo,
        host: BiometricPromptHost?,
        handler: (root: ExtendedPrivateKey?) -> R
    ): R
}

internal class DatabaseAccountStore(
    private val database: Database,
    private val keyStore: KeyStore<AuthenticationContext>,
    private val logger: KLogger
) : AccountStore {
    init {
        database.accountQueries.prune(keyStore.all)
        keyStore.all
            .subtract(database.accountQueries.accountIds().executeAsList())
            .forEach { keyStore.delete(it) }
    }

    private val scope = CoroutineScope(EmptyCoroutineContext)

    override val canStore: Boolean
        get() = keyStore.canStore

    override val accounts: StateFlow<List<Account>>
        get() = database
            .accountQueries
            .accounts()
            .asStateFlow(scope) { it.accounts }

    override fun add(mnemonic: Mnemonic): Account {
        logger.info { "Adding account for mnemonic of length ${mnemonic.length}" }

        val seed = mnemonic.seed()
        val id = SHA256Digest.digest(seed).encodeHex()

        keyStore.generate(id)
        val encryptedSeed = keyStore.encrypt(id, seed)
        val keys = ExtendedPrivateKey.fromSeed(seed).publicKeys(id, index)

        database.transaction {
            database.accountQueries.insertAccount(AccountRecord(id, encryptedSeed))
            keys.forEach { database.accountQueries.insertKey(it) }
        }

        logger.info { "Inserted account $id with ${keys.size} public keys into database" }

        return Account(id, index, keys)
    }

    override fun delete(account: Account) {
        keyStore.delete(account.id)
        database.accountQueries.delete(account.id)
    }

    override fun reset() {
        keyStore.reset()
        database.accountQueries.reset()
    }

    override fun context(account: Account): AuthenticationContext {
        return keyStore.context(account.id)
    }

    override suspend fun <R> authenticate(
        context: AuthenticationContext,
        info: BiometricPromptInfo,
        host: BiometricPromptHost?,
        handler: (root: ExtendedPrivateKey?) -> R
    ): R {
        val encryptedSeed = database.accountQueries.encryptedSeed(context.id).executeAsOneOrNull()
            ?: return handler(null)

        logger.info { "Showing authentication prompt with title ${info.title}" }

        if (!keyStore.prompt(context, host, info)) {
            logger.error { "Authentication prompt was not successful" }
            return handler(null)
        }

        logger.info { "Authentication was successful, decrypting wallet seed" }

        return keyStore.decrypt(context, encryptedSeed) { data ->
            handler(ExtendedPrivateKey.fromSeed(data))
        }
    }

    companion object {
        const val index: Long = 0
    }
}

internal fun ExtendedPrivateKey.publicKeys(id: String, index: Long = 0): List<PublicKeyRecord> {
    return Coin.values().map { coin ->
        PublicKeyRecord(
            id,
            coin,
            index,
            false,
            0,
            child(coin, account = 0, address = 0).publicKey.key
        )
    }
}

internal val Query<PublicKeyRecord>.accounts: List<Account>
    get() = executeAsList()
        .groupBy { it.account_id }
        .mapValues { Account(it.key, DatabaseAccountStore.index, it.value) }
        .values
        .toList()
