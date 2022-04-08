package com.conradkramer.wallet

import com.conradkramer.wallet.data.Ethereum_balance
import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.Data
import com.conradkramer.wallet.ethereum.Quantity
import com.conradkramer.wallet.sql.Database
import com.squareup.sqldelight.runtime.coroutines.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

typealias EthereumBalanceRecord = Ethereum_balance

data class Balance(
    val address: Address,
    val contractAddress: Address?,
    val quantity: Quantity
)

internal val ETHEREUM_ADDRESS = Address.fromString("0x0000000000000000000000000000000000000000")

internal fun EthereumBalanceRecord.toBalance() : Balance = Balance(
    address = address,
    contractAddress = contract_address,
    quantity = balance
)

internal interface BalanceStore {
    fun getBalances(address: Address): Flow<Collection<Balance>>
    fun add(balance: Balance): Balance
}

internal class EthereumBalanceStore(private val database: Database) : BalanceStore {
    override fun getBalances(address: Address) : Flow<Collection<Balance>> = database
        .ethereumBalanceQueries
        .balancesForAddress(address)
        .asFlow()
        .map { query ->
            query.executeAsList()
                .map { it.toBalance() }
        }

    override fun add(balance: Balance) : Balance {
        database.ethereumBalanceQueries.insert(
            balance.address,
            balance.contractAddress ?: ETHEREUM_ADDRESS,
            balance.quantity
        )

        return balance
    }
}
