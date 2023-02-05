package com.conradkramer.wallet.indexing

import com.conradkramer.wallet.Currency
import com.conradkramer.wallet.Locale
import com.conradkramer.wallet.ethereum.indexing.ChainIndexer
import com.conradkramer.wallet.ethereum.types.Chain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.EmptyCoroutineContext

class AppIndexer internal constructor() : KoinComponent {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val scope: CoroutineScope = CoroutineScope(
        EmptyCoroutineContext + Dispatchers.Default.limitedParallelism(5)
    )

    private val chainIndexer: ChainIndexer = getKoin().get { parametersOf(Chain.MAINNET, scope) }
    private val coinbaseIndexer: CoinbaseIndexer = getKoin().get {
        parametersOf(
            scope,
            Locale.current.currencyCode ?: Currency.Code.USD
        )
    }
}
