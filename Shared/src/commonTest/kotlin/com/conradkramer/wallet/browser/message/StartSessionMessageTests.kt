package com.conradkramer.wallet.browser.message

import kotlin.test.Test
import kotlin.test.assertEquals

internal class StartSessionMessageTests() : MessageTests() {
    @Test
    fun testStartSessionMessageSerialization() {
        val message = StartSessionMessage(
            20,
            "https://conradkramer.com",
            Frame.zero,
            session
        )
        val jsonMessage = decode(
            """{
            "id":20,
            "frame":{"x":0,"y":0,"width":0,"height":0},
            "session": {
                "browser_pid": 1,
                "tab_id": 2,
                "frame_id": 3
            },
            "url":"https://conradkramer.com",
            "type":"start_session",
            "payload":{}
        }
            """.trimIndent()
        )

        assertEquals(message, jsonMessage)
    }
}
