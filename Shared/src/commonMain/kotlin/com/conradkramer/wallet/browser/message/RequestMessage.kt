package com.conradkramer.wallet.browser.message

import kotlinx.serialization.Serializable

@Serializable
internal abstract class RequestMessage : Message() {
    abstract val url: String
}
