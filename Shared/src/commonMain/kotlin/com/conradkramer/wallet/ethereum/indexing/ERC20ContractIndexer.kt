package com.conradkramer.wallet.ethereum.indexing

import com.conradkramer.wallet.Currency
import com.conradkramer.wallet.data.Erc20_contract
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.abi.erc20
import com.conradkramer.wallet.ethereum.abi.events.Transfer
import com.conradkramer.wallet.ethereum.requests.JsonRpcError
import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.ethereum.types.Quantity
import com.conradkramer.wallet.sql.Database
import kotlinx.coroutines.CoroutineScope
import mu.KLogger

internal class ERC20ContractIndexer(
    chain: Chain,
    scope: CoroutineScope,
    database: Database,
    private val client: RpcClient,
    logger: KLogger
) : QueryIndexer<Address>(
    chain,
    scope,
    database,
    database.ethereumQueries.erc20ContractsToIndex(chain, Data(Transfer.selector.data)),
    Address::toString,
    logger
) {

    override suspend fun index(identifier: String) {
        val row = Address.fromString(identifier)
        logger.info { "Indexing ERC-20 contract $row" }

        val erc20 = client.erc20(row)

        val totalSupply = try { erc20.totalSupply() } catch (e: JsonRpcError) {
            logger.info { "$row: Fetching totalSupply() failed, it is not an ERC-20 contract" }
            database.ethereumQueries.insertERC20Contract(Erc20_contract(chain, row, null, null, null, null))
            return
        }

        val name = try { erc20.name() } catch (e: JsonRpcError) { null }
        val symbol = try { erc20.symbol() } catch (e: JsonRpcError) { null }
        val decimals = try { erc20.decimals() } catch (e: JsonRpcError) { null }

        logger.info { "$row has \"$name\", \"$symbol\", \"${totalSupply.toLong()}\" and $decimals decimals" }

        database.ethereumQueries.insertERC20Contract(
            Erc20_contract(
                chain,
                row,
                Quantity(totalSupply),
                symbol?.let { Currency.Code(it) },
                decimals?.toLong(),
                name
            )
        )
    }
}
