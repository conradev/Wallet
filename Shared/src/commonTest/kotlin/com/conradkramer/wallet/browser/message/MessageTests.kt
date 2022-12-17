package com.conradkramer.wallet.browser.message

import kotlinx.serialization.json.Json

internal open class MessageTests {
    val session = Session(1, 2, 3)

    protected fun decode(string: String): Message {
        return Message.decodeFromJsonElement(Json.parseToJsonElement(string))
    }
}
