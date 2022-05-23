package com.conradkramer.wallet

internal data class LaunchOptions(
    val reset: Boolean
) {
    companion object {
        val default = LaunchOptions(false)
        val current = launchOptionsForCurrentProcess()
    }
}

internal expect fun launchOptionsForCurrentProcess(): LaunchOptions
