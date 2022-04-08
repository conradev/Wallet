package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.ETHEREUM_ADDRESS

enum class Token(
    val contractAddress: Address,
    val chainId: Int,
    val decimals: Int,
    val displayName: String,
    val symbol: String
) {
    BasicAttentionToken(
        Address.fromString("0x0D8775F648430679A709E98d2b0Cb6250d2887EF"),
        1,
        18,
        "Basic Attention Token",
        "BAT"
    ),
    USDCoin(
        Address.fromString("0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48"),
        1,
        6,
        "USD Coin",
        "USDC"
    ),
    CompoundAugur(
        Address.fromString("0x158079Ee67Fce2f58472A96584A73C7Ab9AC95c1"),
        1,
        8,
        "Compound Augur",
        "cREP"
    );


    companion object {
        private val contractAddressToToken: Map<Address, Token>
            get() = values().associate { (it.contractAddress to it) }

        fun fromAddress(address: Address) = contractAddressToToken[address]
                ?: throw Exception("Invalid Contract Address")
    }
}