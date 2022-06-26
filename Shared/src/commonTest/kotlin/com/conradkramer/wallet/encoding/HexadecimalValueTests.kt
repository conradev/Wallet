package com.conradkramer.wallet.encoding

import com.conradkramer.wallet.bigint.BigDecimal
import com.conradkramer.wallet.ethereum.Quantity
import kotlin.test.Test
import kotlin.test.assertEquals

class HexadecimalValueTests {
    @Test
    fun testEmptyQuantity() {
        val quantity = Quantity.fromString("0x")
        assertEquals(BigDecimal(quantity.value).toDouble(), 0.0)
    }
}
