package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.AccountStore
import com.conradkramer.wallet.BiometricPromptHost
import com.conradkramer.wallet.BiometricPromptInfo
import com.conradkramer.wallet.Coin
import com.conradkramer.wallet.browser.prompt.SignDataPrompt
import com.conradkramer.wallet.ethereum.Data
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import mu.KLogger
import kotlin.coroutines.EmptyCoroutineContext

class SignDataPromptViewModel internal constructor(
    prompt: SignDataPrompt,
    private val accountStore: AccountStore,
    private val host: BiometricPromptHost? = null,
    private val logger: KLogger
) : PromptViewModel<SignDataPrompt, SignDataPrompt.Response>(prompt) {
    val title = "“${prompt.domain}” would like to use your account to sign the following message:"
    val warning = "This could potentially be dangerous. If you do not understand the above message, press “Cancel”"
    val signTitle = "Sign"
    val cancelTitle = "Cancel"

    private val info = BiometricPromptInfo(
        "Sign Data",
        prompt.domain,
        "“${prompt.domain}” would like to sign a message",
        "Cancel"
    )

    val biometryType = accountStore.biometryType

    private val account = accountStore.accounts.value
        .first { it.ethereumAddress == prompt.address }

    val context = accountStore.context(account)

    val data: String = try {
        prompt.data.data.decodeToString(throwOnInvalidSequence = true)
    } catch (e: CharacterCodingException) {
        prompt.data.toString()
    }

    private val scope = CoroutineScope(EmptyCoroutineContext)

    fun sign() {
        scope.launch {
            accountStore.authenticate(context, info, host) { keyOrNull ->
                val rootKey = keyOrNull ?: return@authenticate respond(SignDataPrompt.Response(null))
                val signature = rootKey
                    .child(Coin.ETHEREUM, 0, false, 0)
                    .key
                    .sign(prompt.data.data)
                    .toByteArray()
                    .let(::Data)
                respond(SignDataPrompt.Response(signature))
            }
        }
    }

    override fun cancel() {
        respond(SignDataPrompt.Response(null))
    }
}
