package com.conradkramer.wallet.ethereum.indexing

import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.sql.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import mu.KLogger
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
internal abstract class Indexer(
    val chain: Chain,
    protected val logger: KLogger,
    protected val database: Database
) {
    protected val scope: CoroutineScope = CoroutineScope(
        EmptyCoroutineContext + Dispatchers.Default.limitedParallelism(1)
    )

    fun refresh() {
        scope.launch {
            try { index() } catch (e: Exception) { logger.error { "Failed to index: $e" } }
        }
    }

    abstract suspend fun index()
}
