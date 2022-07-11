package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.data.Eth_block
import com.conradkramer.wallet.data.Eth_transaction
import com.conradkramer.wallet.ethereum.requests.GetBlockByNumber
import com.conradkramer.wallet.ethereum.requests.GetTransactionReceipt
import com.conradkramer.wallet.ethereum.requests.HydratedBlock
import com.conradkramer.wallet.ethereum.requests.Receipt
import com.conradkramer.wallet.sql.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import mu.KLogger
import kotlin.coroutines.EmptyCoroutineContext

internal class ChainIndexer(
    val chain: Chain,
    providers: Set<RpcProvider>,
    private val database: Database,
    private val logger: KLogger
) {
    val clients = providers.map { RpcClient(it.endpointUrl(chain), logger) }
    val address = Address.fromString("0x8A6752a88417e8F7D822DaCaeB52Ed8e6e591c43")
    val blocks = listOf<Long>(14001657, 13726785, 13757030, 13726818, 14266860, 14001617, 14324196).sorted()

    private val scope = CoroutineScope(EmptyCoroutineContext)
    private val topics = setOf(
        ByteArray(12) + address.data,
    )

    init {
        scope.launch { index() }
    }

    suspend fun index() {
        for (block in blocks) {
            index(block)
        }
    }

    suspend fun index(number: Long) {
        val block: HydratedBlock = clients.random().send(GetBlockByNumber(BlockSpecifier.fromNumber(number), true))
        val filter = LogBloomFilter(block.logsBloom.data)

        insert(block)

        val interested = topics
            .map { filter.contains(it) }
            .reduce { r1, r2 -> r1 or r2 }

        if (interested) {
            for (transaction in block.transactions) {
                val receipt: Receipt = clients.random().send(GetTransactionReceipt(transaction.hash))
                println("Received receipt $receipt")
            }
        }
    }

    private fun insert(block: HydratedBlock) {
        val number = block.number.toLong()
        val timestamp = Instant.fromEpochSeconds(block.timestamp.value.toLong())
        database.transaction {
            database.ethereumQueries.insertBlock(Eth_block(chain, number, timestamp))

            for (transaction in block.transactions) {
                database.ethereumQueries.insertTransaction(Eth_transaction(
                    chain,
                    number,
                    transaction.transactionIndex.toLong(),
                    transaction.hash,
                    transaction.from,
                    transaction.to,
                    transaction.value
                ))
            }
        }
    }
}
