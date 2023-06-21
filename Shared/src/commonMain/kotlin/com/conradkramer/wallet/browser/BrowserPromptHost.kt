package com.conradkramer.wallet.browser

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import com.conradkramer.wallet.BiometricPromptHost
import com.conradkramer.wallet.CrossProcessNotifier
import com.conradkramer.wallet.browser.prompt.PermissionPrompt
import com.conradkramer.wallet.browser.prompt.Prompt
import com.conradkramer.wallet.browser.prompt.SignDataPrompt
import com.conradkramer.wallet.sql.Database
import com.conradkramer.wallet.viewmodel.AnyPromptViewModel
import com.conradkramer.wallet.viewmodel.PermissionPromptViewModel
import com.conradkramer.wallet.viewmodel.SignDataPromptViewModel
import io.github.oshai.kotlinlogging.KLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.Koin
import org.koin.core.annotation.Factory
import org.koin.core.component.KoinComponent
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.EmptyCoroutineContext

@Factory
class BrowserPromptHost internal constructor(
    private val database: Database,
    private val logger: KLogger,
) : KoinComponent {
    private val scope = CoroutineScope(EmptyCoroutineContext)
    private val notifier = CrossProcessNotifier()

    var handler: ((Prompt) -> Unit)? = null

    init {
        val host = this
        scope.launch {
            prompts
                .fold(setOf<Prompt>()) { oldValue, newValue ->
                    val unhandledPrompts = newValue.toSet()
                    unhandledPrompts
                        .subtract(oldValue)
                        .forEach { host.handler?.invoke(it) }
                    unhandledPrompts
                }
        }
    }

    val prompts: StateFlow<List<Prompt>>
        get() {
            val query = database.browserPromptQueries.pending()
            val content: (Query<Prompt>) -> List<Prompt> = { it.executeAsList() }
            val notifications = notifier.listen(BrowserPromptExecutor.topic)
            return listOf(query.asFlow(), notifications.map { query })
                .merge()
                .map(content)
                .stateIn(scope, SharingStarted.WhileSubscribed(), content(query))
        }

    fun prompt(id: String): Prompt? {
        return database.browserPromptQueries.prompt(id).executeAsOneOrNull()
    }

    fun viewModel(koin: Koin, prompt: Prompt, host: BiometricPromptHost?): AnyPromptViewModel {
        val viewModelClass = when (prompt) {
            is PermissionPrompt -> PermissionPromptViewModel::class
            is SignDataPrompt -> SignDataPromptViewModel::class
        }
        val viewModel: AnyPromptViewModel = koin.get(viewModelClass) { parametersOf(prompt, host) }
        viewModel.respond = { response ->
            database.browserPromptQueries.respond(response, prompt.id)
            notifier.notify(BrowserPromptExecutor.topic)
            logger.info { "Responded to prompt ${prompt.id} with $response" }
        }
        return viewModel
    }
}
