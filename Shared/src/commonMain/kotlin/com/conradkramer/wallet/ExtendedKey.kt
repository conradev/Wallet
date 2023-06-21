package com.conradkramer.wallet

import com.conradkramer.wallet.crypto.PrivateKey
import com.conradkramer.wallet.crypto.PublicKey
import com.conradkramer.wallet.crypto.RIPEMD160Digest
import com.conradkramer.wallet.crypto.SHA256Digest
import com.conradkramer.wallet.crypto.SHA512Mac
import com.conradkramer.wallet.encoding.encodeBase58Check
import com.conradkramer.wallet.encoding.toByteArray
import io.ktor.utils.io.core.ByteOrder
import io.ktor.utils.io.core.toByteArray

internal enum class Network {
    MAINNET,
    TESTNET,
}

internal abstract class ExtendedKey(
    val network: Network,
    val chainCode: ByteArray,
    val depth: UByte,
    val parentFingerprint: ByteArray,
    val childNumber: UInt,
) {
    protected abstract val encodedKey: ByteArray
    protected abstract val version: UInt

    val encoded: ByteArray
        get() {
            var encoded = ByteArray(0)
            encoded += version.toByteArray(ByteOrder.BIG_ENDIAN)
            encoded += depth.toByte()
            encoded += parentFingerprint.copyOf(4)
            encoded += childNumber.toByteArray(ByteOrder.BIG_ENDIAN)
            encoded += chainCode
            encoded += encodedKey
            return encoded
        }

    val encodedString: String
        get() = encoded.encodeBase58Check()
}

internal class ExtendedPrivateKey(
    network: Network,
    val key: PrivateKey,
    chainCode: ByteArray,
    depth: UByte,
    parentFingerprint: ByteArray,
    childNumber: UInt,
) : ExtendedKey(network, chainCode, depth, parentFingerprint, childNumber) {

    override val encodedKey: ByteArray
        get() = ByteArray(1) + key.encoded

    override val version: UInt
        get() = when (network) {
            Network.MAINNET -> 0x0488ADE4u
            Network.TESTNET -> 0x04358394u
        }

    val publicKey: ExtendedPublicKey
        get() = ExtendedPublicKey(network, key.publicKey, chainCode, depth, parentFingerprint, childNumber)

    fun child(index: UInt, hardened: Boolean): ExtendedPrivateKey {
        return child(if (hardened) { index + 0x80000000u } else { index })
    }

    private fun child(index: UInt): ExtendedPrivateKey {
        var data = if (index >= 0x80000000u) {
            encodedKey
        } else {
            publicKey.key.encoded(true)
        }

        data += index.toByteArray(ByteOrder.BIG_ENDIAN)

        val digest = SHA512Mac.authenticationCode(data, chainCode)

        return ExtendedPrivateKey(
            network,
            key + PrivateKey(digest.copyOf(32)),
            digest.copyOfRange(32, 64),
            depth.inc(),
            publicKey.fingerprint,
            index,
        )
    }

    fun child(coin: Coin, account: Int = 0, change: Boolean = false, address: Int): ExtendedPrivateKey {
        return this
            .child(44u, true)
            .child(coin.number.toUInt(), true)
            .child(account.toUInt(), true)
            .child(if (change) { 1u } else { 0u })
            .child(address.toUInt())
    }

    companion object {
        fun fromSeed(seed: ByteArray, network: Network = Network.MAINNET): ExtendedPrivateKey {
            val root = SHA512Mac.authenticationCode(seed, "Bitcoin seed".toByteArray())
            return ExtendedPrivateKey(
                network,
                PrivateKey(root.copyOf(32)),
                root.copyOfRange(32, 64),
                0u,
                ByteArray(20),
                0u,
            )
        }
    }
}

internal class ExtendedPublicKey(
    network: Network,
    val key: PublicKey,
    chainCode: ByteArray,
    depth: UByte,
    parentFingerprint: ByteArray,
    childNumber: UInt,
) : ExtendedKey(network, chainCode, depth, parentFingerprint, childNumber) {
    override val encodedKey: ByteArray
        get() = key.encoded(true)

    override val version: UInt
        get() = when (network) {
            Network.MAINNET -> 0x0488B21Eu
            Network.TESTNET -> 0x043587CFu
        }

    val fingerprint: ByteArray
        get() = RIPEMD160Digest.digest(SHA256Digest.digest(key.encoded(true)))
}
