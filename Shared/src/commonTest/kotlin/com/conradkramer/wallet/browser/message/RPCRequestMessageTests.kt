package com.conradkramer.wallet.browser.message

import com.conradkramer.wallet.ethereum.requests.GetBalance
import com.conradkramer.wallet.ethereum.types.Address
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RPCRequestMessageTests : MessageTests() {
    @Test
    fun testRequestMessageSerialization() {
        val message = RPCRequestMessage(
            20,
            "https://conradkramer.com",
            Frame.zero,
            session,
            GetBalance(Address.fromString("0x8a6752a88417e8f7d822dacaeb52ed8e6e591c43"))
        )
        val jsonMessage = decode(
            """{
            "id": 20,
            "frame": {"x": 0, "y": 0, "width": 0, "height": 0},
            "session": {
                "browser_pid": 1,
                "tab_id": 2,
                "frame_id": 3
            },
            "url": "https://conradkramer.com",
            "type": "rpc_request",
            "payload": {
                "method":"eth_getBalance",
                "params":["0x8A6752a88417e8F7D822DaCaeB52Ed8e6e591c43", "latest"]
            }
        }
            """.trimIndent()
        )

        assertEquals(message, jsonMessage)
    }
}
