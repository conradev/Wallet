package com.conradkramer.wallet

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/* ktlint-disable max-line-length */
class XPCTests {
    @Test
    fun testXPCJsonConversion() {
        val json = Json.parseToJsonElement(
            """{"string": "value", "int": 123, "double": 1.2342, "bool": false, "array": ["hello", 1, 2], "null": null}"""
        )
        assertEquals(json, json.xpc.json)
    }
}
