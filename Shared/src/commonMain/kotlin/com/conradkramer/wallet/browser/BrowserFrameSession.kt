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
import com.conradkramer.wallet.ethereum.RpcClient
import com.conradkramer.wallet.ethereum.requests.Accounts
import com.conradkramer.wallet.ethereum.requests.ChainId
import com.conradkramer.wallet.ethereum.requests.JsonRpcError
import com.conradkramer.wallet.ethereum.requests.PersonalSign
import com.conradkramer.wallet.ethereum.requests.Request
import com.conradkramer.wallet.ethereum.requests.RequestAccounts
import com.conradkramer.wallet.ethereum.requests.SendTransaction
import com.conradkramer.wallet.ethereum.requests.Sign
import com.conradkramer.wallet.ethereum.requests.SignRequest
import com.conradkramer.wallet.ethereum.requests.SignTransaction
import com.conradkramer.wallet.ethereum.requests.SignTypedData
import com.conradkramer.wallet.ethereum.requests.Subscribe
import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.Data
import com.conradkramer.wallet.ethereum.types.Quantity
import io.github.oshai.kotlinlogging.KLogger
import io.ktor.http.ParametersBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.coroutines.EmptyCoroutineContext

internal class BrowserFrameSession(
    private val rpcClient: RpcClient,
    private val accountStore: AccountStore,
    private val permissionStore: BrowserPermissionStore,
    private val executor: BrowserPromptExecutor,
    private val logger: KLogger,
    private val session: Session,
    private val sender: (Message) -> Unit,
) {
    private val scope = CoroutineScope(EmptyCoroutineContext)

    private val accounts = accountStore.accounts

    init {
        logger.info { "Starting browser session $session" }

        scope.launch {
            try {
                val response: Quantity = rpcClient.send(ChainId())
                send(EventMessage(session, ConnectEvent(response)))
            } catch (e: Exception) {
                logger.error { "Failed to get Chain ID and send the connect event: $e" }
            }
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
            is RPCRequestMessage -> scope.launch { handleRPCMessage(message) }
            else -> {
                logger.info { "Received unexpected message $message" }
            }
        }
    }

    private suspend fun handleRPCMessage(message: RPCRequestMessage) {
        try {
            val request = try {
                message.request
            } catch (e: Exception) {
                throw ProviderError.invalid
            }

            logger.info { "Received ${request.method} RPC request: $request" }

            val response: JsonElement = when (request) {
                is RequestAccounts -> Request.encode(requestAccounts(message))
                is Accounts -> Request.encode(accounts(message))
                is PersonalSign -> Request.encode(sign(request, message))
                is Sign -> Request.encode(sign(request, message))
                is SignTypedData -> unsupported(request)
                is SignTransaction -> unsupported(request)
                is SendTransaction -> unsupported(request)
                is Subscribe -> unsupported(request)
                else -> proxy(request, message)
            }
            send(RPCResponseMessage(message, response))
        } catch (error: JsonRpcError) {
            send(RPCResponseMessage(message, error))
        } catch (e: Exception) {
            logger.error { "Request ${message.id} failed: $e" }
            send(RPCResponseMessage(message, JsonRpcError(0, "An unknown error occurred")))
        }
    }

    private suspend fun prompt(account: Account, message: RPCRequestMessage): Boolean {
        val prompt = PermissionPrompt(message.frame, message.session, account.id, message.domain)
        return when (executor.executePrompt<PermissionPrompt.Response>(prompt)) {
            PermissionPrompt.Response.ALLOW -> {
                permissionStore.allow(account, message.domain)
                true
            }
            PermissionPrompt.Response.DENY -> {
                permissionStore.deny(account, message.domain)
                false
            }
            PermissionPrompt.Response.CANCEL -> false
        }
    }

    private suspend fun proxy(request: Request, message: RPCRequestMessage): JsonElement {
        logger.info { "Proxying request ${message.id}" }
        return rpcClient.send(request)
    }

    private suspend fun requestAccounts(message: RPCRequestMessage): List<Address> {
        accountStore.accounts.value
            .filter { permissionStore.state(it, message.domain) == BrowserPermissionStore.State.UNSPECIFIED }
            .forEach { prompt(it, message) }

        return accounts(message)
    }

    private fun accounts(message: RPCRequestMessage): List<Address> {
        return accountStore.accounts.value
            .filter { permissionStore.state(it, message.domain) == BrowserPermissionStore.State.ALLOWED }
            .map { it.ethereumAddress }
    }

    private suspend fun sign(request: SignRequest, message: RPCRequestMessage): Data {
        val account = accountStore.accounts.value
            .filter { permissionStore.state(it, message.domain) == BrowserPermissionStore.State.ALLOWED }
            .firstOrNull { it.ethereumAddress == request.address } ?: throw ProviderError.unauthorized(message.domain)

        val prompt = SignDataPrompt(
            message.frame,
            message.session,
            message.domain,
            account.ethereumAddress,
            request.data,
        )
        return executor.executePrompt<SignDataPrompt.Response>(prompt).signature ?: throw ProviderError.cancelled
    }

    private fun unsupported(request: Request): JsonElement {
        throw ProviderError.unsupported(request)
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

@Serializable
internal enum class ProviderError(private val code: Int) {
    UNKNOWN(0),
    INVALID_REQUEST(-32600),
    USER_REJECTED_REQUEST(4001),
    UNAUTHORIZED(4100),
    UNSUPPORTED_METHOD(4200),
    DISCONNECTED(4900),
    CHAIN_DISCONNECTED(4901),
    ;

    fun message(message: String, data: JsonElement? = null): JsonRpcError {
        return JsonRpcError(code, message, data)
    }

    companion object {
        val cancelled = USER_REJECTED_REQUEST.message("User cancelled operation")
        val invalid = INVALID_REQUEST.message("The request is invalid")
        fun unauthorized(domain: String) = UNAUTHORIZED.message("$domain is not authorized to perform this operation")
        fun unsupported(request: Request) = UNSUPPORTED_METHOD.message("${request.method} is not supported")
    }
}
