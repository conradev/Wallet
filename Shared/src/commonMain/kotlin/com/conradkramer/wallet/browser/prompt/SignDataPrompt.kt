package com.conradkramer.wallet.browser.prompt

import com.conradkramer.wallet.browser.message.Frame
import com.conradkramer.wallet.browser.message.Session
import com.conradkramer.wallet.encoding.encodeHex
import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.Data
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
@SerialName("eth_sign")
data class SignDataPrompt internal constructor(
    override val id: String,
    override val frame: Frame,
    override val session: Session,
    val domain: String,
    val address: Address,
    val data: Data
) : Prompt() {
    constructor(frame: Frame, session: Session, domain: String, address: Address, data: Data) : this(
        Random.nextBytes(20).encodeHex(),
        frame,
        session,
        domain,
        address,
        data
    )

    @Serializable
    data class Response(
        val signature: Data? = null
    )
}
