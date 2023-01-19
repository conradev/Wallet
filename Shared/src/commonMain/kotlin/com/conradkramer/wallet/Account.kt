@file:Suppress("MemberVisibilityCanBePrivate")

package com.conradkramer.wallet

import com.conradkramer.wallet.crypto.PublicKey
import com.conradkramer.wallet.crypto.ethereumAddress
import com.conradkramer.wallet.data.Public_key
import com.conradkramer.wallet.ethereum.types.Address

internal data class Account(val id: String, private val keys: Map<Coin, List<PublicKey>>) {
    constructor(id: String, index: Long, keys: List<PublicKeyRecord>) : this(
        id,
        keys
            .filter { it.account_index == index }
            .groupBy(
                keySelector = Public_key::coin,
                valueTransform = Public_key::encoded
            )
    )

    init {
        if (keys.isEmpty()) {
            throw Exception("Accounts must have at least one key")
        }
    }

    val ethereumKeys: List<PublicKey>
        get() = keys[Coin.ETHEREUM].orEmpty()

    val primaryEthereumKey: PublicKey
        get() = ethereumKeys.first()

    val ethereumAddress: Address
        get() = primaryEthereumKey.ethereumAddress

    companion object
}
