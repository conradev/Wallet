package com.conradkramer.wallet.encoding

import io.ktor.utils.io.core.ByteOrder
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class UnsignedValueEncodingTests {
    @Test
    fun testUIntEndianness() {
        val input = 168496141u
        val littleEndian = input.toByteArray(ByteOrder.LITTLE_ENDIAN)
        val bigEndian = input.toByteArray(ByteOrder.BIG_ENDIAN)
        assertContentEquals("0d0c0b0a".decodeHex(), littleEndian)
        assertContentEquals("0a0b0c0d".decodeHex(), bigEndian)

        assertEquals(input, littleEndian.toUInt(ByteOrder.LITTLE_ENDIAN))
        assertEquals(input, bigEndian.toUInt(ByteOrder.BIG_ENDIAN))
    }

    @Test
    fun testULongEndianness() {
        val input = 72623859790382856UL
        val littleEndian = input.toByteArray(ByteOrder.LITTLE_ENDIAN)
        val bigEndian = input.toByteArray(ByteOrder.BIG_ENDIAN)
        assertContentEquals("0807060504030201".decodeHex(), littleEndian)
        assertContentEquals("0102030405060708".decodeHex(), bigEndian)

        assertEquals(input, littleEndian.toULong(ByteOrder.LITTLE_ENDIAN))
        assertEquals(input, bigEndian.toULong(ByteOrder.BIG_ENDIAN))
    }

    @Test
    fun testNoPaddingUIntEndianness() {
        val input = 2571u
        val littleEndian = input.toByteArray(ByteOrder.LITTLE_ENDIAN, false)
        val bigEndian = input.toByteArray(ByteOrder.BIG_ENDIAN, false)
        assertContentEquals("0b0a".decodeHex(), littleEndian)
        assertContentEquals("0a0b".decodeHex(), bigEndian)

        assertEquals(input, littleEndian.toUInt(ByteOrder.LITTLE_ENDIAN))
        assertEquals(input, bigEndian.toUInt(ByteOrder.BIG_ENDIAN))
    }

    @Test
    fun testNoPaddingULongEndianness() {
        val input = 2571UL
        val littleEndian = input.toByteArray(ByteOrder.LITTLE_ENDIAN, false)
        val bigEndian = input.toByteArray(ByteOrder.BIG_ENDIAN, false)
        assertContentEquals("0b0a".decodeHex(), littleEndian)
        assertContentEquals("0a0b".decodeHex(), bigEndian)

        assertEquals(input, littleEndian.toULong(ByteOrder.LITTLE_ENDIAN))
        assertEquals(input, bigEndian.toULong(ByteOrder.BIG_ENDIAN))
    }
}
