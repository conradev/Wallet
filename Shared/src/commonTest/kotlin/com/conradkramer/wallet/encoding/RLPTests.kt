package com.conradkramer.wallet.encoding

import com.conradkramer.wallet.encoding.RLP.Item
import io.ktor.utils.io.core.toByteArray
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

/* ktlint-disable max-line-length */
class RLPTests {
    @Test
    fun testSingleByte() {
        val input = "0F".decodeHex()
        val decoded = RLP.decode(input)
        assertEquals(Item.Data(ByteArray(1) { 15 }), decoded)
    }

    @Test
    fun testShortString() {
        val input = "80".decodeHex()
        val decoded = RLP.decode(input)
        val encoded = RLP.encode(decoded)
        assertEquals(Item.Data(ByteArray(0)), decoded)
        assertContentEquals(input, encoded)
    }

    @Test
    fun testLongString() {
        val input = "B8C74C6F72656D20697073756D20646F6C6F722073697420616D65742C20636F6E73656374657475722061646970697363696E6720656C69742E20566573746962756C756D20657420706F727461206469616D2E2053757370656E646973736520696D70657264696574206469676E697373696D2074757270697320612076656E656E617469732E204D6F726269206D61676E61206C616375732C206567657374617320657520636F6E64696D656E74756D2061742C20656C656D656E74756D206575207175616D2E".decodeHex()
        val string = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum et porta diam. Suspendisse imperdiet dignissim turpis a venenatis. Morbi magna lacus, egestas eu condimentum at, elementum eu quam."
        val decoded = RLP.decode(input)
        val encoded = RLP.encode(decoded)
        assertEquals(Item.Data(string.toByteArray()), decoded)
        assertContentEquals(input, encoded)
    }

    @Test
    fun testShortList() {
        val input = "C7C0C1C0C3C0C1C0".decodeHex()
        val decoded = RLP.decode(input)
        val encoded = RLP.encode(decoded)
        val expected = Item.List(
            listOf(
                Item.List(),
                Item.List(
                    listOf(
                        Item.List(),
                    ),
                ),
                Item.List(
                    listOf(
                        Item.List(),
                        Item.List(
                            listOf(
                                Item.List(),
                            ),
                        ),
                    ),
                ),
            ),
        )
        assertEquals(expected, decoded)
        assertContentEquals(input, encoded)
    }

    @Test
    fun testLongList() {
        val input = "F83C846E79616E846E79616E846E79616E846E79616E846E79616E846E79616E846E79616E846E79616E846E79616E846E79616E846E79616E846E79616E".decodeHex()
        val decoded = RLP.decode(input)
        val encoded = RLP.encode(decoded)
        assertEquals(Item.List(List(12) { Item.Data("nyan".toByteArray()) }), decoded)
        assertContentEquals(input, encoded)
    }
}
