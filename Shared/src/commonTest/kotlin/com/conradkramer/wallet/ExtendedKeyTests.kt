package com.conradkramer.wallet

import com.conradkramer.wallet.encoding.decodeHex
import com.conradkramer.wallet.encoding.encodeHex
import kotlin.test.Test
import kotlin.test.assertEquals

/* ktlint-disable max-line-length */
class ExtendedKeyTests {

    // https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki#Test_vector_1
    private val seed = "000102030405060708090a0b0c0d0e0f".decodeHex()

    @Test
    fun testRootKeyGeneration() {
        val key = ExtendedPrivateKey.fromSeed(seed)

        assertEquals("e8f32e723decf4051aefac8e2c93c9c5b214313817cdb01a1494b917c8436b35", key.key.encoded.encodeHex())
        assertEquals("873dff81c02f525623fd1fe5167eac3a55a049de3d314bb42ee227ffed37d508", key.chainCode.encodeHex())
    }

    @Test
    fun testKeySerialization() {
        val key = ExtendedPrivateKey.fromSeed(seed)

        assertEquals("xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNNU3TGtRBeJgk33yuGBxrMPHi", key.encodedString)
    }

    @Test
    fun testPublicKeyGeneration() {
        val key = ExtendedPrivateKey.fromSeed(seed).publicKey

        assertEquals("xpub661MyMwAqRbcFtXgS5sYJABqqG9YLmC4Q1Rdap9gSE8NqtwybGhePY2gZ29ESFjqJoCu1Rupje8YtGqsefD265TMg7usUDFdp6W1EGMcet8", key.encodedString)
    }

    @Test
    fun testHardenedChildDerivation() {
        val key = ExtendedPrivateKey.fromSeed(seed)
        val child = key.child(0u, true)

        assertEquals("xprv9uHRZZhk6KAJC1avXpDAp4MDc3sQKNxDiPvvkX8Br5ngLNv1TxvUxt4cV1rGL5hj6KCesnDYUhd7oWgT11eZG7XnxHrnYeSvkzY7d2bhkJ7", child.encodedString)
    }

    @Test
    fun testChildDerivation() {
        val key = ExtendedPrivateKey.fromSeed(seed)
        val child = key
            .child(0u, true)
            .child(1u, false)

        assertEquals("xprv9wTYmMFdV23N2TdNG573QoEsfRrWKQgWeibmLntzniatZvR9BmLnvSxqu53Kw1UmYPxLgboyZQaXwTCg8MSY3H2EU4pWcQDnRnrVA1xe8fs", child.encodedString)
    }
}
