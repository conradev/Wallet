package com.conradkramer.wallet.browser

import com.conradkramer.wallet.Account
import com.conradkramer.wallet.AccountStore
import com.conradkramer.wallet.browser.event.ConnectEvent
import com.conradkramer.wallet.browser.message.EventMessage
import com.conradkramer.wallet.browser.message.Message
import com.conradkramer.wallet.browser.message.RPCRequestMessage
import com.conradkramer.wallet.browser.message.RPCResponseMessage
import com.conradkramer.wallet.browser.message.RequestMessage
import com.conradkramer.wallet.browser.message.Session
import com.conradkramer.wallet.browser.message.StartSessionMessage
import com.conradkramer.wallet.browser.prompt.PermissionPrompt
import com.conradkramer.wallet.browser.prompt.SignDataPrompt
import com.conradkramer.wallet.ethereum.Quantity
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.requests.Accounts
import com.conradkramer.wallet.ethereum.requests.Call
import com.conradkramer.wallet.ethereum.requests.ChainId
import com.conradkramer.wallet.ethereum.requests.GetBalance
import com.conradkramer.wallet.ethereum.requests.Request
import com.conradkramer.wallet.ethereum.requests.RequestAccounts
import com.conradkramer.wallet.ethereum.requests.Sign
import io.ktor.http.ParametersBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import mu.KLogger
import kotlin.coroutines.EmptyCoroutineContext

internal class BrowserFrameSession(
    private val rpcClient: RpcClient,
    private val accountStore: AccountStore,
    private val permissionStore: BrowserPermissionStore,
    private val executor: BrowserPromptExecutor,
    private val logger: KLogger,
    private val session: Session,
    private val sender: (Message) -> Unit
) {
    private val scope = CoroutineScope(EmptyCoroutineContext)

    init {
        logger.info { "Starting browser session $session" }

        scope.launch {
            val response: Quantity = rpcClient.send(ChainId())
            send(EventMessage(session, ConnectEvent(response)))
        }
    }

    private fun send(message: Message) {
        logger.info { "Sending message $message" }
        sender(message)
    }

    internal fun handle(message: Message) {
        logger.info { "Received message $message" }
        when (message) {
            is StartSessionMessage -> {
                logger.info { "Received start session message" }
            }
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

        scope.launch {
            try {
                val response: JsonElement = if (permissible(request, message)) {
                    when (request) {
                        is RequestAccounts -> accountsRequest(message)
                        is Accounts -> accountsRequest(message)
                        is Sign -> signRequest(request, message)
                        else -> proxyRequest(request)
                    }
                } else {
                    JsonNull
                }
                send(RPCResponseMessage(message, response))
            } catch (e: Exception) {
                logger.error { "Failed to handle RPC message $e" }
            }
        }
    }
    private suspend fun permissible(request: Request, message: RPCRequestMessage): Boolean {
        logger.info { "Checking if request is allowed" }

        if (request.proxied) {
            logger.info { "${request::class.simpleName} is proxied, skipping permission check" }
            return true
        }
        if (request.filtered) {
            logger.info { "${request::class.simpleName} handles permissions on its own, skipping explicit check" }
            return true
        }

        val accounts = request.filter(accountStore.accounts.value)
        val statuses = accounts.associate {
            Pair(
                it,
                permissionStore.state(it, message.domain, BrowserPermission.ACCOUNTS)
            )
        }

        for (status in statuses) {
            when (status.value) {
                BrowserPermission.State.ALLOWED -> continue
                BrowserPermission.State.DENIED -> return false
                BrowserPermission.State.UNSPECIFIED -> {
                    when (permissionPrompt(message)) {
                        PermissionPrompt.Response.ALLOW -> {
                            permissionStore.allow(status.key, message.domain, BrowserPermission.ACCOUNTS)
                        }
                        PermissionPrompt.Response.DENY -> {
                            permissionStore.deny(status.key, message.domain, BrowserPermission.ACCOUNTS)
                            return false
                        }
                        PermissionPrompt.Response.CANCEL -> return false
                    }
                }
            }
        }

        return true
    }

    private suspend fun permissionPrompt(message: RPCRequestMessage): PermissionPrompt.Response {
        return executor.executePrompt(PermissionPrompt(message.frame, message.session, message.domain))
    }
    private suspend fun proxyRequest(request: Request): JsonElement {
        return try {
            logger.info { "Proxying request $request" }
            val response: JsonElement = rpcClient.send(request)
            logger.info { "Received response: $response" }
            response
        } catch (e: Exception) {
            logger.error { "Failed to proxy request: $e" }
            JsonNull
        }
    }

    private fun accountsRequest(message: RPCRequestMessage): JsonElement {
        logger.warn { "Received account request" }

        val filtered = accountStore.accounts.value
            .filter {
                permissionStore.state(it, message.domain, BrowserPermission.ACCOUNTS) == BrowserPermission.State.ALLOWED
            }

        return Request.encode(filtered.map { it.ethereumAddress })
    }

    private suspend fun signRequest(request: Sign, message: RPCRequestMessage): JsonElement {
        val prompt = SignDataPrompt(
            message.frame,
            message.session,
            message.domain,
            request.address,
            request.data
        )
        val response = executor.executePrompt<SignDataPrompt.Response>(prompt)
        val signature = response.signature ?: return JsonNull // TODO: Handle error
        return JsonPrimitive(signature.toString()) // TODO: Do not encode manually
    }
}

private val RequestMessage.domain: String
    get() = URLBuilder(Url(url))
        .also {
            it.user = null
            it.password = null
            it.pathSegments = emptyList()
            it.encodedParameters = ParametersBuilder()
            it.fragment = ""
            it.trailingQuery = false
        }
        .build()
        .toString()

private fun Request.filter(accounts: List<Account>): List<Account> {
    return when (this) {
        is Sign -> accounts.filter { it.ethereumAddress == address }
        else -> accounts
    }
}

private val Request.proxied: Boolean
    get() = when (this) {
        is Call -> true
        is ChainId -> true
        is GetBalance -> true
        else -> false
    }

private val Request.filtered: Boolean
    get() = when (this) {
        is Accounts -> true
        else -> false
    }
