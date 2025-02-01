@file:OptIn(ExperimentalForeignApi::class)

package com.conradkramer.wallet

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import platform.darwin.XPC_TYPE_ARRAY
import platform.darwin.XPC_TYPE_BOOL
import platform.darwin.XPC_TYPE_DICTIONARY
import platform.darwin.XPC_TYPE_DOUBLE
import platform.darwin.XPC_TYPE_INT64
import platform.darwin.XPC_TYPE_NULL
import platform.darwin.XPC_TYPE_STRING
import platform.darwin.xpc_array_append_value
import platform.darwin.xpc_array_apply
import platform.darwin.xpc_array_create_empty
import platform.darwin.xpc_bool_create
import platform.darwin.xpc_bool_get_value
import platform.darwin.xpc_dictionary_apply
import platform.darwin.xpc_dictionary_create_empty
import platform.darwin.xpc_dictionary_set_value
import platform.darwin.xpc_double_create
import platform.darwin.xpc_double_get_value
import platform.darwin.xpc_get_type
import platform.darwin.xpc_int64_create
import platform.darwin.xpc_int64_get_value
import platform.darwin.xpc_null_create
import platform.darwin.xpc_object_t
import platform.darwin.xpc_string_create
import platform.darwin.xpc_string_get_string_ptr

@OptIn(ExperimentalSerializationApi::class)
internal sealed class XPC(val json: Json) {
    fun <T> encodeToXPCObject(serializer: SerializationStrategy<T>, value: T): xpc_object_t {
        return json.encodeToJsonElement(serializer, value).xpc
    }

    fun <T> decodeFromXPCObject(deserializer: DeserializationStrategy<T>, value: xpc_object_t): T {
        return json.decodeFromJsonElement(deserializer, value.json)
    }

    inline fun <reified T> encodeToXPCObject(value: T): xpc_object_t {
        return encodeToXPCObject(json.serializersModule.serializer(), value)
    }

    inline fun <reified T> decodeFromXPCObject(value: xpc_object_t): T =
        decodeFromXPCObject(json.serializersModule.serializer(), value)

    companion object Default : XPC(Json)
}

internal val xpc_object_t.json: JsonElement
    get() = when (val type = xpc_get_type(this)) {
        XPC_TYPE_ARRAY -> {
            val list = mutableListOf<JsonElement>()
            xpc_array_apply(this) { _, value ->
                list += value.json
                return@xpc_array_apply true
            }
            JsonArray(list)
        }
        XPC_TYPE_DICTIONARY -> {
            val map = mutableMapOf<String, JsonElement>()
            xpc_dictionary_apply(this) { key, value ->
                map[key!!.toKString()] = value.json
                return@xpc_dictionary_apply true
            }
            JsonObject(map)
        }
        XPC_TYPE_STRING -> JsonPrimitive(xpc_string_get_string_ptr(this)!!.toKString())
        XPC_TYPE_INT64 -> JsonPrimitive(xpc_int64_get_value(this))
        XPC_TYPE_BOOL -> JsonPrimitive(xpc_bool_get_value(this))
        XPC_TYPE_DOUBLE -> JsonPrimitive(xpc_double_get_value(this))
        XPC_TYPE_NULL -> JsonNull
        else -> throw Exception("Unsupported XPC type $type")
    }

internal val JsonElement.xpc: xpc_object_t
    get() = when (this) {
        is JsonArray -> fold(xpc_array_create_empty()) { array, element ->
            xpc_array_append_value(array, element.xpc)
            return@fold array
        }
        is JsonObject -> asSequence().fold(xpc_dictionary_create_empty()) { dictionary, (key, value) ->
            xpc_dictionary_set_value(dictionary, key, value.xpc)
            return@fold dictionary
        }
        is JsonPrimitive -> if (this.isString) {
            xpc_string_create(content)
        } else {
            when (content) {
                "null" -> xpc_null_create()
                "true" -> xpc_bool_create(true)
                "false" -> xpc_bool_create(false)
                else -> try {
                    xpc_int64_create(content.toLong())
                } catch (e: NumberFormatException) {
                    xpc_double_create(content.toDouble())
                }
            }
        }
    }
