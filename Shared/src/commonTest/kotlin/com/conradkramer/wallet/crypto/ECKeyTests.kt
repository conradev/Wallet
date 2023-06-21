package com.conradkramer.wallet.crypto

import com.conradkramer.wallet.assertDataEquals
import com.conradkramer.wallet.assertDataNotEquals
import com.conradkramer.wallet.bigint.BigInteger
import com.conradkramer.wallet.encoding.decodeHex
import com.conradkramer.wallet.encoding.encodeHex
import io.ktor.utils.io.core.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/* ktlint-disable max-line-length */
class ECKeyTests {

    @Test
    fun testPublicKeyConversion() {
        val key = PrivateKey("e8f32e723decf4051aefac8e2c93c9c5b214313817cdb01a1494b917c8436b35".decodeHex())

        assertEquals("e8f32e723decf4051aefac8e2c93c9c5b214313817cdb01a1494b917c8436b35", key.encoded.encodeHex())

        assertEquals(
            "0339a36013301597daef41fbe593a02cc513d0b55527ec2df1050e2e8ff49c85c2",
            key.publicKey.encoded(true).encodeHex(),
        )
        assertEquals(
            "0439a36013301597daef41fbe593a02cc513d0b55527ec2df1050e2e8ff49c85c23cbe7ded0e7ce6a594896b8f62888fdbc5c8821305e2ea42bf01e37300116281",
            key.publicKey.encoded(false).encodeHex(),
        )
    }

    @Test
    fun testVerification() {
        val privateKey = PrivateKey(SecureRandom.nextBytes(32))
        val publicKey = privateKey.publicKey

        val data = "world".toByteArray()
        val differentData = "Hello ".toByteArray() + data

        val signature = privateKey.sign(data)
        assertTrue { publicKey.verify(data, signature) }
        assertFalse { publicKey.verify(differentData, signature) }
    }

    @Test
    fun testKeyRecovery() {
        val privateKey = PrivateKey(SecureRandom.nextBytes(32))
        val publicKey = privateKey.publicKey

        val data = "Hello world".toByteArray()
        val signature = privateKey.sign(data)
        val result = PublicKey.recover(data, signature)

        assertDataEquals(
            publicKey.encoded(false),
            result.encoded(false),
        )
    }

    @Test
    fun testInvalidSignatureWithDifferentKey() {
        val privateKey = PrivateKey(SecureRandom.nextBytes(32))
        val publicKey: PublicKey = privateKey.publicKey

        val data = "Hello world".toByteArray()
        val invalidSignature = Signature(
            BigInteger(SecureRandom.nextBytes(32)),
            BigInteger(SecureRandom.nextBytes(32)),
            1,
        )

        val result = try { PublicKey.recover(data, invalidSignature) } catch (e: Exception) { null }
        if (result != null) assertDataNotEquals(publicKey.encoded(false), result.encoded(false))
    }

    @Test
    fun testInvalidSignatureWithDifferentData() {
        val privateKey = PrivateKey(SecureRandom.nextBytes(32))
        val publicKey = privateKey.publicKey

        val data = "world".toByteArray()
        val differentData = "Hello ".toByteArray() + data

        val signature = privateKey.sign(data)
        val result = PublicKey.recover(differentData, signature)
        assertDataNotEquals(
            result.encoded(false),
            publicKey.encoded(false),
        )
    }

    @Test
    fun testKeyEquality() {
        val privateKey = PrivateKey(SecureRandom.nextBytes(32))
        val publicKey = privateKey.publicKey

        val clonedPrivateKey = PrivateKey(privateKey.encoded)
        val clonedPublicKey = PublicKey(publicKey.encoded(true))

        assertEquals(privateKey, clonedPrivateKey)
        assertEquals(publicKey, clonedPublicKey)
    }
}
