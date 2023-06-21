package com.conradkramer.wallet.ethereum.indexing

import com.conradkramer.wallet.data.Eth_transaction
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.requests.CompletedTransaction
import com.conradkramer.wallet.ethereum.requests.GetTransactionByHash
import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.sql.Database
import io.github.oshai.kotlinlogging.KLogger
import kotlinx.coroutines.CoroutineScope

internal class TransactionIndexer(
    chain: Chain,
    scope: CoroutineScope,
    database: Database,
    private val client: RpcClient,
    logger: KLogger
) : QueryIndexer<Data>(
    chain,
    scope,
    database,
    database.ethereumQueries.transactionsToIndex(chain),
    Data::toString,
    logger
) {
    override suspend fun index(identifier: String) {
        val request = GetTransactionByHash(Data.fromString(identifier))
        val transaction: CompletedTransaction = client.send(request)
        database.ethereumQueries.insertTransaction(
            Eth_transaction(
                transaction.chainId,
                transaction.blockNumber,
                transaction.transactionIndex.toLong(),
                transaction.hash,
                transaction.from,
                transaction.to,
                transaction.value,
                transaction.input
            )
        )
    }
}
