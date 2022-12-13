package com.conradkramer.wallet.browser

import com.conradkramer.wallet.browser.message.Message
import com.conradkramer.wallet.toByteArray
import com.conradkramer.wallet.toNSData
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSData

fun BrowserMessageHost.send(data: NSData, browserPid: Int) {
    val message = decode(Message.injectBrowserPid(data.toByteArray(), browserPid, logger)) ?: return
    receive(message)
}

fun BrowserMessageHost.setReceiver(receiver: (data: NSData) -> Unit) {
    this.setSender { message: Message ->
        receiver(Json.encodeToString(message).toByteArray().toNSData())
    }
}
