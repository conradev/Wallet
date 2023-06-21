package com.conradkramer.wallet.encoding

import io.ktor.utils.io.core.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

/* ktlint-disable max-line-length */
class Base58Tests {
    @Test
    fun testEncode() {
        val input = "The quick brown fox jumps over the lazy dog".toByteArray()
        assertEquals("7DdiPPYtxLjCD3wA1po2rvZHTDYjkZYiEtazrfiwJcwnKCizhGFhBGHeRdx", input.encodeBase58())
    }

    @Test
    fun testEncodeWithZeroPrefix() {
        val input = ByteArray(5) + "Hello World!".toByteArray()
        assertEquals("111112NEpo7TZRRrLZSi2U", input.encodeBase58())
    }

    @Test
    fun testEncodingPrivateKey() {
        val input = "0488ade4000000000000000000873dff81c02f525623fd1fe5167eac3a55a049de3d314bb42ee227ffed37d50800e8f32e723decf4051aefac8e2c93c9c5b214313817cdb01a1494b917c8436b35e77e9d71".decodeHex()
        assertEquals(
            "xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNNU3TGtRBeJgk33yuGBxrMPHi",
            input.encodeBase58(),
        )
    }
}
