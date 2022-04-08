package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.BiometricPromptHost
import com.conradkramer.wallet.browser.BrowserPromptHost
import com.conradkramer.wallet.browser.prompt.Prompt
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent

class BrowserViewModel internal constructor(private val promptHost: BrowserPromptHost) : KoinComponent {

    val prompts: StateFlow<List<Prompt>> = promptHost.prompts

    fun viewModel(prompt: Prompt, host: BiometricPromptHost?): AnyPromptViewModel {
        return promptHost.viewModel(getKoin(), prompt, host)
    }
}
