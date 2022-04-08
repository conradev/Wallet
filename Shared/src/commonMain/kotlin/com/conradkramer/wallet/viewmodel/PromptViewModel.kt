package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.browser.prompt.Prompt

interface AnyPromptViewModel {
    val id: String
    val prompt: Prompt
    var dismiss: (() -> Unit)?
    var respond: ((String) -> Unit)?

    fun cancel()
}

sealed class PromptViewModel<P : Prompt, R>(final override val prompt: P) : AnyPromptViewModel {
    override val id = prompt.id
    override var dismiss: (() -> Unit)? = null
    override var respond: ((String) -> Unit)? = null
}

inline fun <P : Prompt, reified R> PromptViewModel<P, R>.respond(response: R) {
    respond?.invoke(Prompt.encodeToString(response))
    respond = null
    dismiss?.invoke()
    dismiss = null
}
