package com.conradkramer.wallet.browser.event

import com.conradkramer.wallet.ethereum.Quantity
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.serializersModuleOf

@Serializable
internal abstract class Event {
    abstract val name: String
    abstract val value: JsonElement

    companion object {
        inline fun <reified T> encode(value: T): JsonElement {
            return json.encodeToJsonElement(value)
        }

        @OptIn(ExperimentalSerializationApi::class)
        val json = Json {
            encodeDefaults = true
            explicitNulls = false
            serializersModule = SerializersModule {
                include(serializersModuleOf(Quantity.serializer()))
            }
        }
    }
}
