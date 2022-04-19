@file:Suppress("MemberVisibilityCanBePrivate")

package com.conradkramer.wallet

import com.conradkramer.wallet.data.Public_key
import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.crypto.PublicKey
import com.conradkramer.wallet.crypto.ethereumAddress

internal class Account(val id: String, keys: List<PublicKeyRecord> = listOf()) {

    private val keyMap: Map<Coin, List<PublicKey>> = keys.groupBy(
        keySelector = Public_key::coin,
        valueTransform = Public_key::encoded
    )

    init {
        if (keys.isEmpty()) {
            throw Exception("Accounts must have at least one key")
        }
    }

    val ethereumKeys: List<PublicKey>
        get() = keyMap[Coin.ETHEREUM].orEmpty()

    val primaryEthereumKey: PublicKey
        get() = ethereumKeys.first()

    val ethereumAddress: Address
        get() = primaryEthereumKey.ethereumAddress
}
