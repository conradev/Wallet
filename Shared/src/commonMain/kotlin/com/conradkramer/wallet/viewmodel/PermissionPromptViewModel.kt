package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.browser.prompt.PermissionPrompt

class PermissionPromptViewModel(
    prompt: PermissionPrompt
) : PromptViewModel<PermissionPrompt, PermissionPrompt.Response>(prompt) {
    val title = "“${prompt.domain}” would like to access your:"
    val allowTitle = "Allow"
    val denyTitle = "Don't Allow"
    val permissions = listOf(Permission.ADDRESS, Permission.ACCOUNT_BALANCE, Permission.ACTIVITY)

    fun allow() {
        respond(PermissionPrompt.Response.ALLOW)
    }

    fun deny() {
        respond(PermissionPrompt.Response.DENY)
    }

    override fun cancel() {
        respond(PermissionPrompt.Response.CANCEL)
    }

    enum class Permission {
        ADDRESS,
        ACCOUNT_BALANCE,
        ACTIVITY;

        val summary: String
            get() = when (this) {
                ADDRESS -> "Ethereum address"
                ACCOUNT_BALANCE -> "Account balances and NFTs"
                ACTIVITY -> "Past activity"
            }
    }
}
