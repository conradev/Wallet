package com.conradkramer.wallet.ethereum.indexing

import com.conradkramer.wallet.Currency
import com.conradkramer.wallet.data.Erc721_contract
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.abi.erc721
import com.conradkramer.wallet.ethereum.abi.erc721Enumerable
import com.conradkramer.wallet.ethereum.abi.erc721Metadata
import com.conradkramer.wallet.ethereum.abi.events.Transfer
import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.Chain
import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.ethereum.types.Quantity
import com.conradkramer.wallet.sql.Database
import io.github.oshai.kotlinlogging.KLogger
import kotlinx.coroutines.CoroutineScope

internal class ERC721ContractIndexer(
    chain: Chain,
    scope: CoroutineScope,
    database: Database,
    private val client: RpcClient,
    logger: KLogger,
) : QueryIndexer<Address>(
    chain,
    scope,
    database,
    database.ethereumQueries.erc721ContractsToIndex(chain, Data(Transfer.selector.data)),
    Address::toString,
    logger,
) {

    override suspend fun index(identifier: String) {
        val row = Address.fromString(identifier)
        val erc721 = client.erc721(row)
        if (erc721 == null) {
            logger.info { "$row does not support ERC-721" }
            database.ethereumQueries.insertERC721Contract(Erc721_contract(chain, row, null, null, null))
            return
        }

        logger.info { "Indexing ERC-721 token contract $row" }

        val metadata = client.erc721Metadata(row)
        val (name, symbol) = if (metadata != null) {
            logger.info { "$row supports ERC-721 metadata interface" }

            val name = metadata.name()
            val symbol = metadata.symbol()
            logger.info { "$row has name ($name) and symbol ($symbol)" }

            (name to symbol)
        } else {
            logger.info { "$row does not support ERC-721 metadata interface" }
            (null to null)
        }

        val enumerable = client.erc721Enumerable(row)
        val totalSupply = if (enumerable != null) {
            val totalSupply = enumerable.totalSupply()
            logger.info { "$row has $totalSupply tokens" }
            totalSupply
        } else {
            logger.info { "$row does not support ERC-721 enumerable interface" }
            null
        }

        database.ethereumQueries.insertNonfungibleTokenContract(
            Erc721_contract(
                chain,
                row,
                symbol?.let { Currency.Code(it) },
                name,
                totalSupply?.let(::Quantity),
            ),
        )
    }
}
