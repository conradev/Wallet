package com.conradkramer.wallet.browser.prompt

import com.conradkramer.wallet.browser.message.Frame
import com.conradkramer.wallet.browser.message.PageIdentifier
import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.Data
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("eth_sign")
data class SignDataPrompt internal constructor(
    override val id: String,
    override val frame: Frame,
    override val pageId: PageIdentifier,
    val domain: String,
    val address: Address,
    val data: Data
) : Prompt() {
    @Serializable
    data class Response(
        val signature: Data? = null
    )
}
