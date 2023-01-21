package com.conradkramer.wallet.ethereum.indexing

import app.cash.sqldelight.coroutines.asFlow
import com.conradkramer.wallet.data.Erc20_balance
import com.conradkramer.wallet.data.Eth_balance
import com.conradkramer.wallet.data.Eth_block
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.abi.Type
import com.conradkramer.wallet.ethereum.abi.encode
import com.conradkramer.wallet.ethereum.abi.erc20
import com.conradkramer.wallet.ethereum.abi.events.Transfer
import com.conradkramer.wallet.ethereum.requests.Block
import com.conradkramer.wallet.ethereum.requests.GetBalance
import com.conradkramer.wallet.ethereum.requests.GetBlockByNumber
import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.BlockSpecifier
import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.ethereum.types.Quantity
import com.conradkramer.wallet.sql.Database
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import mu.KLogger

internal class BalanceIndexer(
    val address: Address,
    private val client: RpcClient,
    database: Database,
    chain: Chain,
    logger: KLogger
) : Indexer(chain, logger, database) {

    private val contracts = database.ethereumQueries.erc20ContractsForAccount(
        chain,
        Data(Transfer.selector.data),
        Data(Type.Address.encode(address))
    )

    init {
        scope.launch {
            contracts.asFlow()
                .map { it.executeAsList() }
                .distinctUntilChanged()
                .collect { refresh() }
        }
    }

    override suspend fun index() {
        val result = runCatching {
            val block: Block<Data> = client.send(GetBlockByNumber(hydrated = false))
            database.ethereumQueries.insertBlock(
                Eth_block(
                    chain,
                    block.number.toLong(),
                    Instant.fromEpochSeconds(block.timestamp.toLong(), 0)
                )
            )
            block
        }

        if (result.isFailure) {
            logger.error { "Failed to fetch block when updating $address: ${result.exceptionOrNull()}" }
            return
        }

        val block = result.getOrThrow().number.toLong()

        logger.info { "Updating balances for $address to block $block" }

        updateEthereumBalance(address, block)
        updateTokenBalances(address, block)
    }

    private suspend fun updateEthereumBalance(address: Address, block: Long) {
        val result = runCatching {
            val balance: Quantity = client.send(GetBalance(address, BlockSpecifier.fromNumber(block)))
            database.ethereumQueries.updateBalance(
                Eth_balance(
                    chain,
                    address,
                    balance,
                    block
                )
            )
        }

        if (result.isFailure) {
            logger.error { "Failed to fetch Ethereum balance for $address: ${result.exceptionOrNull()}" }
            return
        }

        logger.info { "Finished updating ETH balance for $address" }
    }

    private suspend fun updateTokenBalances(address: Address, block: Long) {
        val contracts = contracts.executeAsList()
        if (contracts.isEmpty()) return

        val results = contracts.map { contract ->
            scope.async {
                runCatching {
                    val balance = client.erc20(contract).balanceOf(address, BlockSpecifier.fromNumber(block))
                    database.ethereumQueries.updateTokenBalance(
                        Erc20_balance(
                            chain,
                            contract,
                            address,
                            Quantity(balance),
                            block
                        )
                    )
                }
            }
        }
            .awaitAll()
        val successes = results.count { it.isSuccess }

        logger.info { "Finished updating ERC-20 balances for $address ($successes/${results.size})" }
    }
}
