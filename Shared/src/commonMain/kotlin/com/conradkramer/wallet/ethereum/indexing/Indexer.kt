package com.conradkramer.wallet.ethereum.indexing

import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.sql.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mu.KLogger

internal abstract class Indexer(
    val chain: Chain,
    protected val scope: CoroutineScope,
    protected val database: Database,
    protected val logger: KLogger
) {
    fun refresh() {
        scope.launch {
            try { index() } catch (e: Exception) { logger.error { "Failed to index: $e" } }
        }
    }

    abstract suspend fun index()
}
