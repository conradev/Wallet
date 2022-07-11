package com.conradkramer.wallet

import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.Quantity
import com.conradkramer.wallet.ethereum.TokenStore
import com.conradkramer.wallet.sql.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.EmptyCoroutineContext

data class Balance(
    val currency: Currency,
    val quantity: Quantity
)

internal interface BalanceStore {
    val address: Address
    val balances: StateFlow<List<Balance>>
    fun add(currency: Currency, balance: Quantity)
}

internal class DatabaseBalanceStore(
    override val address: Address,
    private val database: Database,
    private val tokenStore: TokenStore
) : BalanceStore {
    private val scope = CoroutineScope(EmptyCoroutineContext)

    override val balances: StateFlow<List<Balance>> = MutableStateFlow(listOf())

    override fun add(currency: Currency, balance: Quantity) {
    }

    companion object {
        val ETHEREUM_ADDRESS = Address.fromString("0x0000000000000000000000000000000000000000")
    }
}
