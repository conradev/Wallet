package com.conradkramer.wallet.viewmodel

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class OnboardingViewModel {
    enum class Screen {
        WELCOME,
        IMPORT_PHRASE,
        GENERATE;

        companion object
    }

    val screens = MutableStateFlow(listOf(Screen.WELCOME))

    private val scope = MainScope()

    fun import() {
        scope.launch {
            screens.compareAndSet(listOf(Screen.WELCOME), listOf(Screen.WELCOME, Screen.IMPORT_PHRASE))
        }
    }

    fun back() {
        scope.launch {
            if (screens.value.size > 1) {
                screens.value = screens.value.dropLast(1)
            }
        }
    }
}
