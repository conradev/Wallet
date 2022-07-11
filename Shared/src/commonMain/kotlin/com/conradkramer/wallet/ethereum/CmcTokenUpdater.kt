package com.conradkramer.wallet.ethereum

import kotlinx.coroutines.CoroutineScope
import mu.KLogger
import kotlin.coroutines.EmptyCoroutineContext

internal interface TokenUpdater {
    fun update()
}

internal class CmcTokenUpdater(
    private val client: CmcClient,
    private val tokenStore: TokenStore,
    private val logger: KLogger
) : TokenUpdater {
    private val scope = CoroutineScope(EmptyCoroutineContext)

    override fun update() {
    }
}
