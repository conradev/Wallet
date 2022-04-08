package com.conradkramer.wallet.browser

import com.conradkramer.wallet.Account
import com.conradkramer.wallet.AccountStore
import com.conradkramer.wallet.browser.message.Message
import com.conradkramer.wallet.browser.message.OpenURLMessage
import com.conradkramer.wallet.browser.message.PageIdentifier
import com.conradkramer.wallet.browser.message.RPCRequestMessage
import com.conradkramer.wallet.browser.message.RPCResponseMessage
import com.conradkramer.wallet.browser.prompt.PermissionPrompt
import com.conradkramer.wallet.browser.prompt.Prompt
import com.conradkramer.wallet.browser.prompt.SignDataPrompt
import com.conradkramer.wallet.encoding.encodeHex
import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.requests.Accounts
import com.conradkramer.wallet.ethereum.requests.Sign
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import mu.KLogger
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.random.Random

class BrowserMessageHost internal constructor(
    private val client: RpcClient,
    private val accountStore: AccountStore,
    private val permissionStore: BrowserPermissionStore,
    private val executor: BrowserPromptExecutor,
    private val logger: KLogger
) {

    private val scope = CoroutineScope(EmptyCoroutineContext)
    private var sender: ((Message) -> Unit)? = null

    init {
        executor.reset()
    }

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
        logger.info { "Received message $message" }

        when (message) {
            is RPCRequestMessage -> handleRPCMessage(message)
            else -> {
                logger.info { "Received unexpected message $message" }
            }
        }
    }

    private fun handleRPCMessage(message: RPCRequestMessage) {
        val request = try { message.request } catch (e: Exception) {
            logger.error { "Failed to deserialize request: $e" }
            return
        }
        logger.info { "Received ${request.method} RPC request: $request" }

        when (request) {
            is Accounts -> scope.launch { handleAccountsRequest(message) }
            is Sign -> scope.launch { handleSignRequest(message, request) }
            else -> scope.launch {
                try {
                    logger.info { "Received other RPC request $request, proxying" }
                    val response: JsonElement = client.send(message.request)
                    logger.info { "Received RPC response: $response" }
                    send(RPCResponseMessage(message, response))
                } catch (e: Exception) {
                    logger.error { "Failed to proxy RPC request: $e" }
                }
            }
        }
    }

    private suspend fun handleAccountsRequest(message: RPCRequestMessage) {
        logger.warn { "Received account request" }

        val domain = Url(message.url).host
        val result = if (checkPermission(message, domain, Accounts.method)) {
            val accounts = accountStore.accounts.value
                .map(Account::ethereumAddress)
                .map(Address::toString)
            // TODO: Do not encode manually
            JsonArray(accounts.map(::JsonPrimitive))
        } else {
            // TODO: Send permission error
            JsonArray(listOf())
        }

        send(RPCResponseMessage(message, result))
    }

    private suspend fun handleSignRequest(message: RPCRequestMessage, request: Sign) {
        val accounts = accountStore.accounts.value.map(Account::ethereumAddress)
        if (!accounts.contains(request.address)) {
            // TODO: Handle
            return
        }

        val domain = Url(message.url).host
        val prompt = SignDataPrompt(
            "${message.id}",
            message.frame,
            PageIdentifier(message),
            domain,
            request.address,
            request.data
        )
        val response = executor.executePrompt<SignDataPrompt.Response>(prompt)
        val signature = response.signature ?: return
        val result = JsonPrimitive(signature.toString()) // TODO: Do not encode manually
        send(RPCResponseMessage(message, result))
    }

    private suspend fun checkPermission(message: RPCRequestMessage, domain: String, permission: String): Boolean {
        return when (permissionStore.state(domain, permission)) {
            BrowserPermissionStore.State.ALLOWED -> {
                logger.info { "Domain $domain has $permission permission" }
                true
            }
            BrowserPermissionStore.State.DENIED -> {
                logger.info { "Domain $domain does not have $permission permission" }
                false
            }
            BrowserPermissionStore.State.UNSPECIFIED -> {
                logger.info { "Domain $domain has no permission state for $permission, prompting" }

                val id = Random.nextBytes(20).encodeHex()
                val prompt = PermissionPrompt(id, message.frame, PageIdentifier(message), domain, listOf(permission))
                when (executor.executePrompt<PermissionPrompt.Response>(prompt)) {
                    PermissionPrompt.Response.ALLOW -> {
                        permissionStore.allow(domain, permission)
                        logger.info { "Granted $domain permission for $permission" }
                        true
                    }
                    PermissionPrompt.Response.DENY -> {
                        permissionStore.deny(domain, permission)
                        logger.info { "Denied $domain permission for $permission" }
                        false
                    }
                    PermissionPrompt.Response.CANCEL -> {
                        logger.info { "Prompt cancelled for $permission permission on $domain" }
                        false
                    }
                }
            }
        }
    }

    fun openURL(url: String, prompt: Prompt) {
        send(OpenURLMessage(prompt, url))
    }
}
