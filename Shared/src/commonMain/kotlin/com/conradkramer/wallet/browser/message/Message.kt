@file:OptIn(ExperimentalSerializationApi::class)

package com.conradkramer.wallet.browser.message

import io.github.oshai.kotlinlogging.KLogger
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
@JsonClassDiscriminator("type")
internal sealed class Message {
    abstract val id: Long
    abstract val session: Session

    fun encodeToJsonElement(): JsonElement {
        return json.encodeToJsonElement(serializer(), this)
    }

    companion object {
        internal fun injectBrowserPid(data: ByteArray, pid: Int, logger: KLogger): JsonElement {
            val message = Json.parseToJsonElement(data.decodeToString())
                .jsonObject
                .toMap()

            val session = message["session"]
                ?.let { json.decodeFromJsonElement<Session>(it) }
                ?.browserPid(pid)
                ?.let { json.encodeToJsonElement(it) }

            return if (session != null) {
                JsonObject(message + ("session" to session))
            } else {
                logger.error { "Failed to annotate message with browser PID $pid" }
                JsonObject(message)
            }
        }

        private val json = Json {
            serializersModule = SerializersModule {
                polymorphic(Message::class) {
                    subclass(EventMessage::class)
                    subclass(OpenURLMessage::class)
                    subclass(RPCRequestMessage::class)
                    subclass(RPCResponseMessage::class)
                    subclass(StartSessionMessage::class)
                }
            }
        }

        fun decodeFromJsonElement(element: JsonElement): Message {
            return json.decodeFromJsonElement(serializer(), element)
        }
    }
}
