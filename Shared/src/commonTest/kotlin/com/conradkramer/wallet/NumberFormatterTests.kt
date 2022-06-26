package com.conradkramer.wallet

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberFormatterTests {

    @Test
    fun testBasicFormatting() {
        val string = NumberFormatter.testing.string(8435.321404)
        assertEquals("8,435.32", string)
    }
}
