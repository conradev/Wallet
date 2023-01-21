package com.conradkramer.wallet

internal data class LaunchOptions(
    val resetAccounts: Boolean,
    val resetIndex: Boolean
) {
    companion object {
        val default = LaunchOptions(false, false)
        val current = launchOptionsForCurrentProcess()
    }
}

internal expect fun launchOptionsForCurrentProcess(): LaunchOptions
