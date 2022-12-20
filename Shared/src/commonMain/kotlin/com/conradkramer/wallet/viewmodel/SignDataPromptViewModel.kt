package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.Account
import com.conradkramer.wallet.AccountStore
import com.conradkramer.wallet.BiometricPromptHost
import com.conradkramer.wallet.BiometricPromptInfo
import com.conradkramer.wallet.Coin
import com.conradkramer.wallet.browser.prompt.SignDataPrompt
import com.conradkramer.wallet.ethereum.signMessage
import kotlinx.coroutines.CoroutineScope
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
        "sign a message for “${prompt.domain}”",
        "Cancel"
    )

    val data: String = try {
        prompt.data.data.decodeToString(throwOnInvalidSequence = true)
    } catch (e: CharacterCodingException) {
        prompt.data.toString()
    }

    private val account: Account?
        get() = accountStore.accounts.value.firstOrNull { it.ethereumAddress == prompt.address }

    val context = account?.let { accountStore.context(it) }

    private val scope = CoroutineScope(EmptyCoroutineContext)

    fun sign() {
        if (context == null) {
            respond(SignDataPrompt.Response(null))
            return
        }

        scope.launch {
            val signature = accountStore.authenticate(context, info, host) { keyOrNull ->
                val rootKey = keyOrNull ?: return@authenticate null
                val ethereumKey = rootKey
                    .child(Coin.ETHEREUM, 0, false, 0)
                    .key
                ethereumKey.signMessage(prompt.data.data)
            }
            respond(SignDataPrompt.Response(signature))
        }
    }

    override fun cancel() {
        respond(SignDataPrompt.Response(null))
    }
}
