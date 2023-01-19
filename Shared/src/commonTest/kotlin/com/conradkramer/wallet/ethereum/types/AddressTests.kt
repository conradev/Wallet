package com.conradkramer.wallet.ethereum.types

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class AddressTests {
    @Test
    fun testValidChecksum() {
        val address = Address.fromString("0x8A6752a88417e8F7D822DaCaeB52Ed8e6e591c43")
        assertEquals("0x8A6752a88417e8F7D822DaCaeB52Ed8e6e591c43", address.toString())
    }

    @Test
    fun testLowercase() {
        val address = Address.fromString("0x8a6752a88417e8f7d822dacaeb52ed8e6e591c43")
        assertEquals("0x8A6752a88417e8F7D822DaCaeB52Ed8e6e591c43", address.toString())
    }

    @Test
    fun testUppercase() {
        val address = Address.fromString("0x8A6752A88417E8F7D822DACAEB52ED8E6E591C43")
        assertEquals("0x8A6752a88417e8F7D822DaCaeB52Ed8e6e591c43", address.toString())
    }

    @Test
    fun testInvalidFormat() {
        assertFails {
            Address.fromString("0x8A6752a88417e8F7D822DaCaeB52ed8e6e591c")
        }
    }

    @Test
    fun testInvalidChecksum() {
        assertFails {
            Address.fromString("0x8A6752a88417e8F7D822DaCaeB52ed8e6e591c43")
        }
    }
}
