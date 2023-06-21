package com.conradkramer.wallet.ethereum.indexing

import com.conradkramer.wallet.data.Eth_account_transaction
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.requests.alchemy.GetAssetTransfers
import com.conradkramer.wallet.ethereum.requests.alchemy.all
import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.BlockSpecifier
import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.sql.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import io.github.oshai.kotlinlogging.KLogger

internal class AccountTransactionIndexer(
    chain: Chain,
    scope: CoroutineScope,
    database: Database,
    private val client: RpcClient,
    val address: Address,
    logger: KLogger
) : Indexer(chain, scope, database, logger) {
    init { refresh() }

    override suspend fun index() {
        val fromBlock = database.ethereumQueries
            .lastIndexedBlockForAccount(chain, address)
            .executeAsOne()
            .block
            ?.let { BlockSpecifier.fromNumber(it + 1) }

        logger.info { "Enumerating transactions for $address from block ${fromBlock ?: "0"}" }

        val transactions = listOf(
            GetAssetTransfers(fromBlock = fromBlock, fromAddress = address),
            GetAssetTransfers(fromBlock = fromBlock, toAddress = address)
        )
            .map { scope.async { client.all(it) } }
            .awaitAll()
            .flatten()
            .map { it.block to it.hash }
            .toSet()

        if (transactions.isEmpty()) {
            logger.info { "Did not find new transactions for $address" }
            return
        }

        logger.info { "Found ${transactions.size} new transactions for $address" }

        database.transaction {
            for (transaction in transactions) {
                database.ethereumQueries.insertTransactionForAccount(
                    Eth_account_transaction(
                        chain,
                        address,
                        transaction.first,
                        transaction.second
                    )
                )
            }
        }
    }
}
