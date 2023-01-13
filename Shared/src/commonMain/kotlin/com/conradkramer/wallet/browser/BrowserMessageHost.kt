package com.conradkramer.wallet.browser

import com.conradkramer.wallet.browser.message.Message
import com.conradkramer.wallet.browser.message.OpenURLMessage
import com.conradkramer.wallet.browser.message.Session
import com.conradkramer.wallet.browser.prompt.Prompt
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.logger
import kotlinx.serialization.json.JsonElement
import mu.KLogger
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent

@Single
class BrowserMessageHost internal constructor(
    private val client: RpcClient,
    internal val logger: KLogger
) : KoinComponent {

    private var sender: ((Message) -> Unit)? = null
    private val sessions: MutableMap<Session, BrowserFrameSession> = mutableMapOf()

    internal fun setSender(sender: (Message) -> Unit) {
        this.sender = sender
    }

    private fun send(message: Message) {
        sender?.invoke(message)
        logger.info { "Sent message $message" }
    }

    internal fun decode(element: JsonElement): Message? {
        return try {
            Message.decodeFromJsonElement(element)
        } catch (e: Exception) {
            logger.error { "Unable to decode message: $e" }
            null
        }
    }

    internal fun receive(message: Message) {
        session(message).handle(message)
    }

    private fun session(message: Message): BrowserFrameSession {
        return sessions.getOrPut(message.session) {
            BrowserFrameSession(
                client,
                getKoin().get(),
                getKoin().get(),
                getKoin().get(),
                getKoin().logger<BrowserFrameSession>(),
                message.session
            ) { send(it) }
        }
    }

    fun openURL(prompt: Prompt, url: String) {
        send(OpenURLMessage(prompt.session, url))
    }
}
