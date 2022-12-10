package com.conradkramer.wallet.browser.message

import com.conradkramer.wallet.browser.prompt.Prompt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
@SerialName("open_url")
internal data class OpenURLMessage(
    override val id: Long,
    override val frame: Frame,
    @SerialName("frame_id")
    override val frameId: String,
    @SerialName("browser_pid")
    override val browserPid: Int,
    val payload: Payload
) : Message() {
    constructor(prompt: Prompt, url: String) : this(
        Random.nextLong(),
        Frame.zero,
        prompt.pageId.frameId,
        prompt.pageId.browserPid,
        Payload(url)
    )

    @Serializable
    internal data class Payload(
        val url: String
    )
}
