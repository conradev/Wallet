package com.conradkramer.wallet.ethereum.requests

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal data class RequestPermissions(
    val permissions: Set<Permission>,
) : Request() {
    constructor(params: List<JsonElement>) : this(params.map(::Permission).toSet())

    override val method = Companion.method
    override val params: List<JsonElement>
        get() = permissions.map(Permission::encoded)

    data class Permission(
        val name: String,
    ) {
        constructor(input: JsonElement) : this(input.jsonObject.keys.first())

        val encoded: JsonElement
            get() = JsonObject(mapOf(Pair(name, JsonObject(emptyMap()))))
    }

    companion object {
        const val method = "wallet_requestPermissions"
    }
}
