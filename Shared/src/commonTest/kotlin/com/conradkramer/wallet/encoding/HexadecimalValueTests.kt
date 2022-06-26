package com.conradkramer.wallet.encoding

import com.conradkramer.wallet.ethereum.Quantity
import kotlin.test.Test
import kotlin.test.assertEquals

class HexadecimalValueTests {
    @Test
    fun testEmptyQuantity() {
        val quantity = Quantity.fromString("0x").toLong()
        assertEquals(quantity, 0)
    }
}
