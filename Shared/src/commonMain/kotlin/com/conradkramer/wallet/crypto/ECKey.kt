package com.conradkramer.wallet.crypto

import com.conradkramer.wallet.ethereum.Address

internal expect class PrivateKey(data: ByteArray) {
    val publicKey: PublicKey
    val encoded: ByteArray

    operator fun plus(increment: PrivateKey): PrivateKey
}

expect class PublicKey(data: ByteArray) {
    fun encoded(compressed: Boolean = true): ByteArray
}

internal val PublicKey.ethereumAddress: Address
    get() {
        val digest = Keccak256Digest().digest(encoded(false).copyOfRange(1, 65))
        return Address(digest.copyOfRange(digest.size - 20, digest.size))
    }
