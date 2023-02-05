package com.conradkramer.wallet

import com.conradkramer.wallet.ethereum.types.Quantity
import kotlin.test.Test
import kotlin.test.assertEquals

class BalanceTests {
    @Test
    fun testDoubleConversion() {
        val balance = Balance(
            Currency(
                Currency.Code.ETH,
                "Ethereum",
                "ETH",
                18
            ),
            Quantity.fromString("0x14f952c14b42909e").value
        )
        assertEquals(1.511, balance.toDouble(), 0.00001)
    }
}
