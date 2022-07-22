package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.encoding.decodeHex
import com.conradkramer.wallet.encoding.encodeHex
import com.conradkramer.wallet.ethereum.abi.Invocation
import com.conradkramer.wallet.ethereum.abi.Type
import com.conradkramer.wallet.ethereum.abi.encode
import kotlin.test.Test
import kotlin.test.assertEquals

class InvocationTests {

    @Test
    fun testFunctionEncoding() {
        val data = Invocation("supportsInterface")
            .parameter(Type.Bytes(4)) { encode("80ac58cd".decodeHex()) }
            .build()

        assertEquals("01ffc9a780ac58cd00000000000000000000000000000000000000000000000000000000", data.encodeHex())
    }
}
