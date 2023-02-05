package com.conradkramer.wallet

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberFormatterTests {

    @Test
    fun testBasicFormatting() {
        val locale = Locale.testing
        val currency = locale.currency(Locale.testing.currencyCode!!)!!
        val string = NumberFormatter.fiat(currency, locale).string(8435.321404)
        assertEquals("$8,435.32", string)
    }
}
