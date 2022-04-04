package com.conradkramer.wallet.platform

import kotlin.test.Test
import kotlin.test.assertEquals

class BitSetTests {
    @Test
    fun testToSeedByteArray() {
        val bitSet = BitSet(12)
        for (index in 0 until 11) {
            bitSet.set(index, true)
        }

        val byteArray = bitSet.toSeedByteArray()

        assertEquals(255.toByte(), byteArray[0])
        assertEquals(224.toByte(), byteArray[1])
    }
}
