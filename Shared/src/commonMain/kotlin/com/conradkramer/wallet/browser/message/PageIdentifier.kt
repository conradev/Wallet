package com.conradkramer.wallet.browser.message

import kotlinx.serialization.Serializable

@Serializable
data class PageIdentifier(
    val frameId: String,
    val browserPid: Int
) {
    internal constructor(message: Message) : this(message.frameId, message.browserPid)
}
