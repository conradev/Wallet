package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.BiometricPromptHost
import com.conradkramer.wallet.browser.BrowserPromptHost
import com.conradkramer.wallet.browser.prompt.Prompt
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Factory
import org.koin.core.component.KoinComponent

@Factory
class BrowserViewModel internal constructor(private val promptHost: BrowserPromptHost) : KoinComponent {

    @NativeCoroutinesState
    val prompts: StateFlow<List<Prompt>> = promptHost.prompts

    fun viewModel(prompt: Prompt, host: BiometricPromptHost?): AnyPromptViewModel {
        return promptHost.viewModel(getKoin(), prompt, host)
    }
}
