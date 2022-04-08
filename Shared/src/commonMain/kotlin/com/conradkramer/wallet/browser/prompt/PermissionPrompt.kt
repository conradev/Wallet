package com.conradkramer.wallet.browser.prompt

import com.conradkramer.wallet.browser.message.Frame
import com.conradkramer.wallet.browser.message.PageIdentifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("permission")
data class PermissionPrompt internal constructor(
    override val id: String,
    override val frame: Frame,
    override val pageId: PageIdentifier,
    val domain: String,
    val permissions: List<String>
) : Prompt() {
    @Serializable
    enum class Response {
        ALLOW,
        DENY,
        CANCEL
    }
}
