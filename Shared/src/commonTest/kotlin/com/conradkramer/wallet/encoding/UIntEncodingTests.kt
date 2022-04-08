package com.conradkramer.wallet.encoding

import io.ktor.utils.io.core.ByteOrder
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class UIntEncodingTests {
    @Test
    fun testEndianness() {
        val input = 168496141u
        val littleEndian = input.toByteArray(ByteOrder.LITTLE_ENDIAN)
        val bigEndian = input.toByteArray(ByteOrder.BIG_ENDIAN)
        assertContentEquals("0d0c0b0a".decodeHex(), littleEndian)
        assertContentEquals("0a0b0c0d".decodeHex(), bigEndian)

        assertEquals(input, littleEndian.toUInt(ByteOrder.LITTLE_ENDIAN))
        assertEquals(input, bigEndian.toUInt(ByteOrder.BIG_ENDIAN))
    }

    @Test
    fun testNoPaddingEndianness() {
        val input = 2571u
        val littleEndian = input.toByteArray(ByteOrder.LITTLE_ENDIAN, false)
        val bigEndian = input.toByteArray(ByteOrder.BIG_ENDIAN, false)
        assertContentEquals("0b0a".decodeHex(), littleEndian)
        assertContentEquals("0a0b".decodeHex(), bigEndian)

        assertEquals(input, littleEndian.toUInt(ByteOrder.LITTLE_ENDIAN))
        assertEquals(input, bigEndian.toUInt(ByteOrder.BIG_ENDIAN))
    }
}
