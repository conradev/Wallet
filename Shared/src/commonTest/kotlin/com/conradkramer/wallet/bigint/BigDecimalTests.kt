package com.conradkramer.wallet.bigint

import com.conradkramer.wallet.encoding.decodeHex
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class BigDecimalTests {
    @Test
    fun testIntegerZeroSerialization() {
        val empty = ByteArray(0)
        val zero = ByteArray(1) { 0 }
        val integer = BigInteger(zero)

        assertContentEquals(empty, integer.data)
    }

    @Test
    fun testIntegerSerialization() {
        val data = "1bceedbd796a74fb831".decodeHex(true)
        val integer = BigInteger(data)

        assertContentEquals(data, integer.data)
    }

    @Test
    fun testIntegerBasicMath() {
        val data = "1bceedbd796a74fb831".decodeHex(true)
        val balance = BigDecimal(BigInteger(data)) / BigDecimal.valueOf(1e18)

        assertEquals(8207.56595, balance.toDouble(), 0.00001)
    }
}
