package com.conradkramer.wallet.ethereum.types

import com.conradkramer.wallet.assertDataEquals
import com.conradkramer.wallet.bigint.BigInteger
import com.conradkramer.wallet.crypto.PrivateKey
import com.conradkramer.wallet.crypto.SecureRandom
import kotlin.test.Test
import kotlin.test.assertEquals

class EnvelopeTests {

    private val envelope = Envelope1559(
        BigInteger.valueOf(1L),
        BigInteger.valueOf(29L),
        BigInteger.valueOf(1000000000L),
        BigInteger.valueOf(14000000000L),
        BigInteger.valueOf(115322L),
        Address.fromString("0xed975db5192ab41713f0080e7306e08188e53e7f"),
        BigInteger.valueOf(0),
        Data.fromString("0xefef39a10000000000000000000000000000000000000000000000000000000000000001"),
    )

    @Test
    fun testSerialization() {
        val privateKey = PrivateKey(SecureRandom.nextBytes(32))
        val publicKey = privateKey.publicKey

        val data = privateKey.signEnvelope(envelope)
        val result = Envelope.verify(data)

        assertEquals(envelope, result.first)
        assertDataEquals(
            publicKey.encoded(false),
            result.second.encoded(false),
        )
    }
}
