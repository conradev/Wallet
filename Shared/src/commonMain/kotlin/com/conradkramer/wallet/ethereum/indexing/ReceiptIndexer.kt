package com.conradkramer.wallet.ethereum.indexing

import com.conradkramer.wallet.data.Eth_log
import com.conradkramer.wallet.data.Eth_receipt
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.requests.GetTransactionReceipt
import com.conradkramer.wallet.ethereum.requests.Receipt
import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.sql.Database
import io.github.oshai.kotlinlogging.KLogger
import kotlinx.coroutines.CoroutineScope

internal class ReceiptIndexer(
    chain: Chain,
    scope: CoroutineScope,
    database: Database,
    private val client: RpcClient,
    logger: KLogger
) : QueryIndexer<Data>(
    chain,
    scope,
    database,
    database.ethereumQueries.receiptsToIndex(chain),
    Data::toString,
    logger
) {
    override suspend fun index(identifier: String) {
        val hash = Data.fromString(identifier)
        val request = GetTransactionReceipt(hash)
        val receipt: Receipt = client.send(request)
        database.transaction {
            database.ethereumQueries.insertReceipt(
                Eth_receipt(
                    chain,
                    receipt.transactionHash,
                    receipt.gasUsed.toLong(),
                    receipt.status.toBool(),
                    receipt.contractAddress
                )
            )
            for (log in receipt.logs) {
                database.ethereumQueries.insertLog(
                    Eth_log(
                        chain,
                        log.transactionHash,
                        log.logIndex.toLong(),
                        log.address,
                        log.topics.elementAtOrNull(0),
                        log.topics.elementAtOrNull(1),
                        log.topics.elementAtOrNull(2),
                        log.topics.elementAtOrNull(3),
                        log.data
                    )
                )
            }
        }
    }
}
