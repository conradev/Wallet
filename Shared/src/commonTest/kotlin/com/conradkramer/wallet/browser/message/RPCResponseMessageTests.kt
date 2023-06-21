package com.conradkramer.wallet.browser.message

import com.conradkramer.wallet.ethereum.requests.GetBalance
import com.conradkramer.wallet.ethereum.requests.JsonRpcError
import com.conradkramer.wallet.ethereum.types.Address
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RPCResponseMessageTests() : MessageTests() {

    private val request = RPCRequestMessage(
        20,
        "https://conradkramer.com",
        Frame.zero,
        session,
        GetBalance(Address.fromString("0x8a6752a88417e8f7d822dacaeb52ed8e6e591c43")),
    )

    @Test
    fun testSuccessMessageSerialization() {
        val message = RPCResponseMessage(
            21,
            request,
            JsonPrimitive("0x1"),
        )
        val jsonMessage = decode(
            """{
            "id": 21,
            "session": {
                "browser_pid": 1,
                "tab_id": 2,
                "frame_id": 3
            },
            "type": "rpc_response",
            "payload": {
                "request_id": 20,
                "result": "0x1"
            }
        }
            """.trimIndent(),
        )

        assertEquals(message, jsonMessage)
    }

    @Test
    fun testErrorMessageSerialization() {
        val message = RPCResponseMessage(
            21,
            request,
            JsonRpcError(-100, "error", JsonPrimitive(1)),
        )
        val jsonMessage = decode(
            """{
            "id": 21,
            "session": {
                "browser_pid": 1,
                "tab_id": 2,
                "frame_id": 3
            },
            "type": "rpc_response",
            "payload": {
                "request_id": 20,
                "error": {
                    "code": -100,
                    "message": "error",
                    "data": 1
                }
            }
        }
            """.trimIndent(),
        )

        assertEquals(message, jsonMessage)
    }
}
