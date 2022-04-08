@file:OptIn(ExperimentalSerializationApi::class)

package com.conradkramer.wallet.browser.message

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
@JsonClassDiscriminator("type")
internal sealed class Message {
    abstract val id: Long
    abstract val frame: Frame
    abstract val frameId: String
    abstract val browserPid: Int

    fun encodeToJsonElement(): JsonElement {
        return json.encodeToJsonElement(serializer(), this)
    }

    companion object {
        private val json = Json {
            serializersModule = SerializersModule {
                polymorphic(Message::class) {
                    subclass(RPCRequestMessage::class)
                    subclass(RPCResponseMessage::class)
                }
            }
        }

        fun decodeFromJsonElement(element: JsonElement): Message {
            return json.decodeFromJsonElement(serializer(), element)
        }
    }
}
