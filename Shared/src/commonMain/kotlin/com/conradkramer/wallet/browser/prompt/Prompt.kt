@file:OptIn(ExperimentalSerializationApi::class)

package com.conradkramer.wallet.browser.prompt

import com.conradkramer.wallet.browser.message.Frame
import com.conradkramer.wallet.browser.message.Session
import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.Data
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.serializersModuleOf
import kotlinx.serialization.modules.subclass

@Serializable
@JsonClassDiscriminator("type")
sealed class Prompt {
    abstract val id: String
    abstract val frame: Frame
    abstract val session: Session

    fun encodeToString(): String {
        return json.encodeToString(serializer(), this)
    }

    companion object {
        val json = Json {
            serializersModule = SerializersModule {
                include(serializersModuleOf(Session.serializer()))
                include(serializersModuleOf(Address.serializer()))
                include(serializersModuleOf(Data.serializer()))
                include(serializersModuleOf(PermissionPrompt.Response.serializer()))
                polymorphic(Prompt::class) {
                    subclass(SignDataPrompt::class)
                    subclass(PermissionPrompt::class)
                }
            }
        }

        inline fun <reified T> encodeToString(value: T): String {
            return json.encodeToString(value)
        }

        inline fun <reified T> decodeFromString(string: String): T {
            return json.decodeFromString(string)
        }
    }
}
