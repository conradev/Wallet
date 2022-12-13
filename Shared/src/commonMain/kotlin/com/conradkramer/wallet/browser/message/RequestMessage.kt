package com.conradkramer.wallet.browser.message

import kotlinx.serialization.Serializable

@Serializable
data class Frame(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double
) {
    companion object {
        val zero = Frame(0.0, 0.0, 0.0, 0.0)
    }
}

@Serializable
internal abstract class RequestMessage : Message() {
    abstract val url: String
    abstract val frame: Frame
}
