package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.JsonRpcRequest
import com.conradkramer.wallet.ethereum.Request
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class GetBalanceTests {
    @Test
    fun testSerialization() {
        val request = GetBalance(Address.fromString("0x8A6752a88417e8F7D822DaCaeB52Ed8e6e591c43"))
        val jsonRpcRequest = Json.decodeFromString<JsonRpcRequest>("""{"method":"eth_getBalance", "id": 2, "params": ["0x8A6752a88417e8F7D822DaCaeB52Ed8e6e591c43"], "jsonrpc": "2.0"}""")
        val deserialized = Request.fromRequest(jsonRpcRequest)
        assertEquals(request, deserialized)
    }
}
