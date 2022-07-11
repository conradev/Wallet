package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.Currency

object Ethereum : Currency {
    override val decimals = 20
    override val displayName = "Ethereum"
    override val symbol = "ETH"
}

data class Token(
    val contractAddress: Address,
    val chainId: Int,
    override val displayName: String,
    override val symbol: String
) : Currency {
    override val decimals = 18
}
