package com.conradkramer.wallet.browser.message

import kotlin.test.Test
import kotlin.test.assertEquals

internal class OpenURLMessageTests() : MessageTests() {
    @Test
    fun testOpenURLMessageSerialization() {
        val message = OpenURLMessage(
            20,
            session,
            "https://conradkramer.com",
        )
        val jsonMessage = decode(
            """{
            "id": 20,
            "session": {
                "browser_pid": 1,
                "tab_id": 2,
                "frame_id": 3
            },
            "type":"open_url",
            "payload": {
                "url": "https://conradkramer.com"
            }
        }
            """.trimIndent(),
        )

        assertEquals(message, jsonMessage)
    }
}
