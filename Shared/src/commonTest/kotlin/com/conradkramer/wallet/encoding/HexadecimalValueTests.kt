package com.conradkramer.wallet.encoding

import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.ethereum.types.Quantity
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class HexadecimalValueTests {
    @Test
    fun testEmptyQuantity() {
        val quantity = Quantity.fromString("0x").toLong()
        assertEquals(quantity, 0)
    }

    @Test
    fun testLargeData() {
        // This used to cause stack exhaustion in Kotlin/Native due to the recursive nature of the engine and expression
        val input = "0x${Random.nextBytes(500 * 1024).encodeHex()}"
        val result = Data.fromString(input)

        assertEquals(input.length / 2 - 1, result.data.size)
    }
}
