package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.data.Eth_block
import com.conradkramer.wallet.data.Eth_token_transfer
import com.conradkramer.wallet.data.Eth_transaction
import com.conradkramer.wallet.encoding.decodeHex
import com.conradkramer.wallet.ethereum.requests.Call
import com.conradkramer.wallet.ethereum.requests.GetBlockByNumber
import com.conradkramer.wallet.ethereum.requests.GetTransactionReceipt
import com.conradkramer.wallet.ethereum.requests.HydratedBlock
import com.conradkramer.wallet.ethereum.requests.Log
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
//    val blocks = listOf<Long>(14001657, 13726785, 13757030, 13726818, 14266860, 14001617, 14324196).sorted()
    val blocks = listOf<Long>(13757030)

    private val scope = CoroutineScope(EmptyCoroutineContext)
    private val topics = listOf(
        ByteArray(12) + address.data,
    )

    init {
        scope.launch { index() }
    }

    suspend fun index() {
//        for (block in blocks) {
//            index(block)
//        }

        val contracts = database.ethereumQueries.tokenContractsToIndex().executeAsList()
        for (contract in contracts) {
            index(contract)
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
            for (transaction in block.transactions.filter { it.hash.toString() == "0xcc0eceb059d65760634519b79b245d4bb396b468f0500750279720a5ed72822e" }) {
                val receipt: Receipt = clients.random().send(GetTransactionReceipt(transaction.hash))
                insert(receipt)
            }
        }
    }

    private suspend fun index(contract: Address) {
        val result: Quantity = clients.random().send(
            Call(
                Transaction(
                    to = contract,
                    data = ABI.supportsInterface("01ffc9a7")
                )
            )
        )

        if (result.value.toLong() == 0L) {
            return
        }

        val result2: Quantity = clients.random().send(
            Call(
                Transaction(
                    to = contract,
                    data = ABI.supportsInterface("ffffffff")
                )
            )
        )

        if (result2.value.toLong() == 1L) {
            return
        }

        val is721: Quantity = clients.random().send(
            Call(
                Transaction(
                    to = contract,
                    data = ABI.supportsInterface("80ac58cd")
                )
            )
        )

        if (is721.value.toLong() == 1L) {
            val supportsMetadata: Quantity = clients.random().send(
                Call(
                    Transaction(
                        to = contract,
                        data = ABI.supportsInterface("5b5e139f")
                    )
                )
            )

            if (supportsMetadata.value.toLong() == 1L) {
                val nameData: Data = clients.random().send(
                    Call(
                        Transaction(
                            to = contract,
                            data = Data.fromString("0x06fdde03")
                        )
                    )
                )
                val name = ABI.decodeString(nameData)
                println("$name")
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

    private fun insert(receipt: Receipt) {
        database.transaction {
            val logs = receipt.logs
                .filter { log -> log.topics.find { topic -> topics.find { topic.data.contentEquals(it) } != null } != null }

            for (log in logs) {
                val from = Address(log.topics[1].data.copyOfRange(12, 32))
                val to = Address(log.topics[2].data.copyOfRange(12, 32))
                database.ethereumQueries.insertTokenTransfer(Eth_token_transfer(
                    chain,
                    log.blockNumber.toLong(),
                    log.transactionIndex.toLong(),
                    log.logIndex.toLong(),
                    log.address,
                    from,
                    to,
                    Quantity(log.data.data)
                ))
            }
        }
    }
}
