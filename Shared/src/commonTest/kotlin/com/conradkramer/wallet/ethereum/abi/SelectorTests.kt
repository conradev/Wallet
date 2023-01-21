package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.assertDataEquals
import com.conradkramer.wallet.encoding.decodeHex
import com.conradkramer.wallet.ethereum.abi.events.Transfer
import kotlin.test.Test

class SelectorTests {

    @Test
    fun testEventSelector() {
        val actual = Transfer.selector.data
        val expected = "ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef".decodeHex()
        assertDataEquals(expected, actual)
    }
}
