package com.conradkramer.wallet.browser

import app.cash.sqldelight.coroutines.asFlow
import com.conradkramer.wallet.CrossProcessNotifier
import com.conradkramer.wallet.FILE_NAME
import com.conradkramer.wallet.browser.prompt.Prompt
import com.conradkramer.wallet.sql.Database
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import mu.KLogger
import org.koin.core.annotation.Factory

@Factory
internal class BrowserPromptExecutor constructor(
    private val database: Database,
    private val logger: KLogger
) {
    private val notifier = CrossProcessNotifier()

    fun reset() {
        database.browserPromptQueries.reset()
    }

    suspend fun executePromptEncoded(prompt: Prompt): String {
        logger.info { "Executing prompt $prompt" }

        database.browserPromptQueries.insert(prompt)
        notifier.notify(topic)

        val query = database.browserPromptQueries.response(prompt.id)
        val notifications = notifier.listen(topic)
        val response = listOf(query.asFlow(), notifications.map { query })
            .merge()
            .mapNotNull { it.executeAsOneOrNull()?.response }
            .first()

        database.browserPromptQueries.clear(prompt.id)
        notifier.notify(topic)

        logger.info { "Received response to prompt ${prompt.id}: $response" }

        return response
    }

    suspend inline fun <reified T> executePrompt(prompt: Prompt): T {
        return Prompt.decodeFromString(executePromptEncoded(prompt))
    }

    companion object {
        val topic = "${BrowserPromptExecutor::class.qualifiedName}.${Database.FILE_NAME}"
    }
}
