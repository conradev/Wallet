package com.conradkramer.wallet

import platform.Foundation.NSProcessInfo

internal actual fun launchOptionsForCurrentProcess(): LaunchOptions {
    val default = LaunchOptions.default
    val arguments = NSProcessInfo
        .processInfo
        .arguments
        .mapNotNull { it as? String }
        .toTypedArray()

    val resetAccounts = if (arguments.contains("--reset-accounts")) true else default.resetAccounts
    val resetIndex = if (arguments.contains("--reset-index")) true else default.resetIndex
    return LaunchOptions(resetAccounts, resetIndex)
}
