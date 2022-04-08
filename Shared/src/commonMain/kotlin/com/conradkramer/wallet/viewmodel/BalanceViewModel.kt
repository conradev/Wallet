package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.AccountStore
import com.conradkramer.wallet.BalanceStore
import com.conradkramer.wallet.BalanceUpdater
import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.Quantity
import com.conradkramer.wallet.ethereum.Token
import com.conradkramer.wallet.platform.BigDecimal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.toString(decimals: Int) : String {
    val integerDigits = this.toInt()
    val floatDigits = ((this - integerDigits) * 10f.pow(decimals)).roundToInt()
    return "${integerDigits}.${floatDigits}"
}

@FlowPreview
class BalanceViewModel internal constructor(
    private val balanceStore: BalanceStore,
    private val accountStore: AccountStore,
    private val ethereumBalanceUpdater: BalanceUpdater
) {
    data class RowViewModel(
        val displayName: String,
        val symbol: String,
        val balance: String
    ) {
        constructor(token: Token, quantity: Quantity) : this(
            token.displayName,
            token.symbol,
            balanceString(quantity, token.decimals, token.symbol)
        )

        companion object {
            fun balanceString(quantity: Quantity, decimals: Int, symbol: String) : String =
                (BigDecimal(quantity.value) / BigDecimal.valueOf(10.0.pow(decimals)))
                    .toDouble()
                    .toString(decimals)
                    .let { "$it $symbol" }

            fun eth(quantity: Quantity): RowViewModel = RowViewModel(
                "Ethereum",
                "ETH",
                balanceString(quantity, 18, "ETH")
            )

            fun address(contractAddress: Address, quantity: Quantity)  = RowViewModel(
                Token.fromAddress(contractAddress),
                quantity
            )
        }
    }

    private val viewModelScope = CoroutineScope(EmptyCoroutineContext)

    init {
        viewModelScope.launch {
            accountStore
                .accountsFlow
                .map { it.first() }
                .map { it.ethereumAddress }
                .collectLatest { ethereumBalanceUpdater.update(it) }
        }
    }

    val walletTitle: Flow<String> = accountStore.accountsFlow
        .map { it.first() }
        .map { it.ethereumAddress.toString() }

    val walletBalances: Flow<Collection<RowViewModel>> = accountStore.accountsFlow
        .map { it.first() }
        .map { it.ethereumAddress }
        .flatMapMerge { balanceStore.getBalances(it) }
        .map { it.reversed() }
        .map { balances ->
            balances.map { balance ->
                balance.contractAddress
                    ?.let { RowViewModel.address(it, balance.quantity) }
                    ?: RowViewModel.eth(balance.quantity)
            }
        }
}