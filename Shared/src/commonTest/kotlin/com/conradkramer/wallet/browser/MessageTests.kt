package com.conradkramer.wallet.browser

import com.conradkramer.wallet.browser.message.Frame
import com.conradkramer.wallet.browser.message.Message
import com.conradkramer.wallet.browser.message.RPCRequestMessage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

/* ktlint-disable max-line-length */
class MessageTests {
    @Test
    fun testRPCMessageSerialization() {
        val message = RPCRequestMessage(
            20,
            Frame.zero,
            "frame",
            "https://conradkramer.com",
            20,
            RPCRequestMessage.Payload(
                "eth_getBalance",
                listOf(
                    JsonPrimitive("test")
                )
            )
        )
        val jsonMessage = Message.decodeFromString(
            """{"id":20,"frame":{"x":0,"y":0,"width":0,"height":0},"frame_id":"frame","url":"https://conradkramer.com","browser_pid":20,"type":"rpc_request","payload":{"method":"eth_getBalance","params":["test"]}}"""
        )

        assertEquals(message, jsonMessage)
    }
}

private fun Message.Companion.decodeFromString(string: String): Message {
    return decodeFromJsonElement(Json.parseToJsonElement(string))
}
