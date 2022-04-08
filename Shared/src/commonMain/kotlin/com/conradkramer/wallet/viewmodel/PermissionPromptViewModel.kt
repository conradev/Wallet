package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.browser.prompt.PermissionPrompt
import com.conradkramer.wallet.ethereum.requests.Accounts

class PermissionPromptViewModel(
    prompt: PermissionPrompt
) : PromptViewModel<PermissionPrompt, PermissionPrompt.Response>(prompt) {
    val title = "“${prompt.domain}” would like to access your:"
    val allowTitle = "Allow"
    val denyTitle = "Don't Allow"
    val permissions = prompt.permissions.flatMap {
        mapping[it] ?: throw Exception("Unrecognized permission $it")
    }

    fun allow() {
        respond(PermissionPrompt.Response.ALLOW)
    }

    fun deny() {
        respond(PermissionPrompt.Response.DENY)
    }

    override fun cancel() {
        respond(PermissionPrompt.Response.CANCEL)
    }

    companion object {
        private val mapping = mapOf(
            Accounts.method to listOf(Permission.ADDRESS, Permission.ACCOUNT_BALANCE, Permission.ACTIVITY)
        )
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
