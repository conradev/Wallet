package com.conradkramer.wallet.crypto

import com.conradkramer.wallet.encoding.decodeHex
import com.conradkramer.wallet.encoding.encodeHex
import io.ktor.utils.io.core.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/* ktlint-disable max-line-length */
class ECKeyTests {

    private val key = PrivateKey("e8f32e723decf4051aefac8e2c93c9c5b214313817cdb01a1494b917c8436b35".decodeHex())

    @Test
    fun testPublicKeyConversion() {
        assertEquals("e8f32e723decf4051aefac8e2c93c9c5b214313817cdb01a1494b917c8436b35", key.encoded.encodeHex())

        assertEquals(
            "0339a36013301597daef41fbe593a02cc513d0b55527ec2df1050e2e8ff49c85c2",
            key.publicKey.encoded(true).encodeHex()
        )
        assertEquals(
            "0439a36013301597daef41fbe593a02cc513d0b55527ec2df1050e2e8ff49c85c23cbe7ded0e7ce6a594896b8f62888fdbc5c8821305e2ea42bf01e37300116281",
            key.publicKey.encoded(false).encodeHex()
        )
    }

    @Test
    fun testSignature() {
        val data = "Hello world".toByteArray()
        val signature = key.sign(data)
        val result = key.publicKey.verify(data, signature)
        assertTrue(result)
    }
}
