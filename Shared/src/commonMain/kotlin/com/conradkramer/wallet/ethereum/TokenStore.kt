package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.sql.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.EmptyCoroutineContext

internal class TokenStore(
    val database: Database
) {
    private val scope = CoroutineScope(EmptyCoroutineContext)

    val tokens: StateFlow<Map<Address, Token>> = MutableStateFlow(mapOf())
}
