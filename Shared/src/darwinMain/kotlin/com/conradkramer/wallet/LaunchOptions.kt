package com.conradkramer.wallet

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import platform.Foundation.NSProcessInfo

internal actual fun launchOptionsForCurrentProcess(): LaunchOptions {
    val default = LaunchOptions.default
    val arguments = NSProcessInfo
        .processInfo
        .arguments
        .mapNotNull { it as? String }
        .toTypedArray()

    val parser = ArgParser(Wallet.localizedAppName, skipExtraArguments = true)
    val resetAccounts by parser.option(ArgType.Boolean, fullName = "reset-accounts").default(default.resetAccounts)
    val resetIndex by parser.option(ArgType.Boolean, fullName = "reset-index").default(default.resetIndex)
    parser.parse(arguments)
    return LaunchOptions(resetAccounts, resetIndex)
}
