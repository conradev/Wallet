package com.conradkramer.wallet.ethereum

import kotlinx.serialization.Serializable

@Serializable
internal data class Transaction(
    val from: Address? = null,
    val to: Address,
    val gas: Quantity? = null,
    val gasPrice: Quantity? = null,
    val value: Quantity? = null,
    val data: Data? = null,
    val nonce: Quantity? = null
)
