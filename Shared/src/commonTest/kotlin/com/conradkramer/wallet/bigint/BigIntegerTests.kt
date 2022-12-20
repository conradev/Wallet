package com.conradkramer.wallet.bigint

import com.conradkramer.wallet.encoding.decodeHex
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class BigIntegerTests {
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
    fun testIntegerToLongConversion() {
        val integer = BigInteger("519828f8e33dd14".decodeHex(true))
        val long = integer.toLong()

        assertEquals(367468397685103892L, long)
    }

    @Test
    fun testIntegerToULongConversion() {
        val integer = BigInteger("ffffffffffffffff".decodeHex())
        val long = integer.toULong()

        assertEquals(18446744073709551615UL, long)
    }

    @Test
    fun testLongToIntegerConversion() {
        val integer = BigInteger.valueOf(367468397685103892L)
        val data = "519828f8e33dd14".decodeHex(true)

        assertContentEquals(data, integer.data)
    }

    @Test
    fun testULongToIntegerConversion() {
        val integer = BigInteger.valueOf(18446744073709551615UL)
        val data = "ffffffffffffffff".decodeHex(true)

        assertContentEquals(data, integer.data)
    }

    @Test
    fun testIntegerEquality() {
        val integer = BigInteger("519828f8e33dd14".decodeHex(true))
        val other = BigInteger.valueOf(367468397685103892L)

        assertEquals(integer, other)
    }
}
