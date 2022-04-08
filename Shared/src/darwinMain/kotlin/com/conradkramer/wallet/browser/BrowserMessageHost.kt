package com.conradkramer.wallet.browser

import com.conradkramer.wallet.browser.message.Message
import com.conradkramer.wallet.toByteArray
import com.conradkramer.wallet.toNSData
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import platform.Foundation.NSData

fun BrowserMessageHost.send(data: NSData, browserPid: Int) {
    val message = decode(data.toByteArray().insertBrowserPid(browserPid)) ?: return
    receive(message)
}

fun BrowserMessageHost.setReceiver(receiver: (data: NSData) -> Unit) {
    this.setSender { message: Message ->
        receiver(Json.encodeToString(message).toByteArray().toNSData())
    }
}

internal fun ByteArray.insertBrowserPid(browserPid: Int): JsonElement {
    val message = Json.parseToJsonElement(decodeToString())
        .jsonObject
        .toMutableMap()
    message["browser_pid"] = JsonPrimitive(browserPid)
    return JsonObject(message)
}
