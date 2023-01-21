package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.assertDataEquals
import com.conradkramer.wallet.ethereum.abi.events.Transfer
import com.conradkramer.wallet.ethereum.types.Data
import kotlin.test.Test

class EventsTests {

    @Test
    fun testEventSignatures() {
        assertDataEquals(
            Data.fromString("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef").data,
            Transfer.selector.data
        )
    }
}
