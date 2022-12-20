package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.Address
import com.conradkramer.wallet.ethereum.Data
import com.conradkramer.wallet.ethereum.Transaction
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/* ktlint-disable max-line-length */
class CallTests {
    @Test
    fun testSerialization() {
        val request = Call(
            Transaction(
                from = Address.fromString("0x8A6752a88417e8F7D822DaCaeB52Ed8e6e591c43"),
                data = Data(ByteArray(5))
            )
        )
        val jsonRpcRequest = Json.decodeFromString<JsonRpcRequest>(
            """{"method":"eth_call", "id": 2, "params": [{"from": "0x8A6752a88417e8F7D822DaCaeB52Ed8e6e591c43", "data": "0x0000000000"}, "latest"], "jsonrpc": "2.0"}"""
        )

        assertEquals(request, jsonRpcRequest.request)
    }
}
