package com.conradkramer.wallet.crypto

import com.conradkramer.wallet.encoding.encodeHex
import io.ktor.utils.io.core.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

/* ktlint-disable max-line-length */
class DigestTests {
    @Test
    fun testKeccakDigest() {
        val data = "testing".toByteArray()
        val digest = Keccak256Digest.digest(data)

        assertEquals("5f16f4c7f149ac4f9510d9cf8cf384038ad348b3bcdc01915f95de12df9d1b02", digest.encodeHex())
    }

    @Test
    fun testRIPEMD160Digest() {
        val data = "testing".toByteArray()
        val digest = RIPEMD160Digest.digest(data)

        assertEquals("b89ba156b40bed29a5965684b7d244c49a3a769b", digest.encodeHex())
    }

    @Test
    fun testSHA512Mac() {
        val data = "testing".toByteArray()
        val key = "test".toByteArray()
        val digest = SHA512Mac.authenticationCode(data, key)

        assertEquals("57916e136fc942282b352c1bf86f01b1ae7599cb451dfbd0dc61e955e496c6d7ee83ba9a8ef8da9f0f922764de2f63a0b6495f908ff74db8b66650a2e8698bdd", digest.encodeHex())
    }
}
