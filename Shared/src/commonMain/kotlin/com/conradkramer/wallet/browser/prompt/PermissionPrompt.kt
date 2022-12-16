package com.conradkramer.wallet.browser.prompt

import com.conradkramer.wallet.browser.message.Frame
import com.conradkramer.wallet.browser.message.Session
import com.conradkramer.wallet.encoding.encodeHex
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
@SerialName("permission")
data class PermissionPrompt internal constructor(
    override val id: String,
    override val frame: Frame,
    override val session: Session,
    val account: String,
    val domain: String
) : Prompt() {
    constructor(frame: Frame, session: Session, account: String, domain: String) : this(
        Random.nextBytes(20).encodeHex(),
        frame,
        session,
        account,
        domain
    )

    @Serializable
    enum class Response {
        ALLOW,
        DENY,
        CANCEL
    }
}
