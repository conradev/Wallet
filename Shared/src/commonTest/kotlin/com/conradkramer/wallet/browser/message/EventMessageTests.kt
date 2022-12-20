package com.conradkramer.wallet.browser.message

import com.conradkramer.wallet.bigint.BigInteger
import com.conradkramer.wallet.browser.event.ConnectEvent
import com.conradkramer.wallet.ethereum.Quantity
import kotlin.test.Test
import kotlin.test.assertEquals

internal class EventMessageTests() : MessageTests() {
    @Test
    fun testEventMessageSerialization() {
        val message = EventMessage(
            20,
            session,
            ConnectEvent(Quantity(BigInteger.valueOf(1)))
        )
        val jsonMessage = decode(
            """{
            "id":20,
            "session": {
                "browser_pid": 1,
                "tab_id": 2,
                "frame_id": 3
            },
            "type":"event",
            "payload": {
                "name": "connect",
                "value": "0x1"
            }
        }
            """.trimIndent()
        )

        assertEquals(message, jsonMessage)
    }
}
