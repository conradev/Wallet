package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.bigint.BigInteger
import kotlinx.serialization.Serializable

@Serializable
internal data class Transaction(
    val from: Address? = null,
    val to: Address? = null,
    val gas: Quantity? = null,
    val gasPrice: Quantity? = null,
    val value: Quantity? = null,
    val data: Data? = null,
    val nonce: Quantity? = null
) {
    init {
        if (nonce != null && Quantity(BigInteger.valueOf(nonce.toULong())) != nonce) {
            throw Exception("Nonce value is too large")
        }
    }
}
