package com.conradkramer.wallet.ethereum.indexing

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.sql.Database
import io.github.oshai.kotlinlogging.KLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal abstract class QueryIndexer<RowType : Any>(
    chain: Chain,
    scope: CoroutineScope,
    database: Database,
    val query: Query<RowType>,
    val identifier: (RowType) -> String,
    logger: KLogger,
) : Indexer(chain, scope, database, logger) {

    private val tasks: MutableMap<String, Deferred<Result<Unit>>> = mutableMapOf()
    private val mutex = Mutex()

    private val identifiers: List<String>
        get() = query.executeAsList().map(identifier)

    init {
        scope.launch {
            query.asFlow()
                .map { identifiers }
                .distinctUntilChanged()
                .collect { scope.launch { index() } }
        }
    }

    override suspend fun index() {
        val identifiers = identifiers

        val additions = mutex.withLock { identifiers.toSet().subtract(tasks.keys) }
        if (additions.isEmpty()) {
            logger.debug { "Nothing to index" }
            return
        }

        if (additions.size == identifiers.size) {
            logger.info { "Indexing ${identifiers.size} items" }
        } else {
            logger.info { "Indexing an additional ${additions.size} items" }
        }

        val results = identifiers
            .map { indexIfNecessaryAsync(it) }
            .awaitAll()
        val successes = results.count { it.isSuccess }

        logger.info { "Finished indexing ($successes/${results.size}) items" }
    }

    private suspend fun indexIfNecessaryAsync(identifier: String): Deferred<Result<Unit>> {
        val start: () -> Deferred<Result<Unit>> = {
            scope.async {
                logger.info { "Indexing $identifier" }
                val result = runCatching { index(identifier) }

                val error = result.exceptionOrNull()
                if (error == null) {
                    logger.info { "Succesfully indexed $identifier" }
                } else {
                    logger.error { "Error indexing $identifier: $error ${error.stackTraceToString()}" }
                }

                mutex.withLock { tasks.remove(identifier) }
                    ?.cancelAndJoin()

                result
            }
        }
        return mutex.withLock { tasks.getOrPut(identifier, start) }
    }

    abstract suspend fun index(identifier: String)
}
