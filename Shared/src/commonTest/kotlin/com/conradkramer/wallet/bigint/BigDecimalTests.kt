package com.conradkramer.wallet.bigint

import com.conradkramer.wallet.encoding.decodeHex
import kotlin.test.Test
import kotlin.test.assertEquals

class BigDecimalTests {
    @Test
    fun testBigDecimalDivision() {
        val data = "1bceedbd796a74fb831".decodeHex(true)
        val balance = BigDecimal(BigInteger(data)) / BigDecimal.valueOf(1e18)

        assertEquals(8207.56595, balance.toDouble(), 0.00001)
    }
}
