package com.conradkramer.wallet.ethereum.types

import com.conradkramer.wallet.bigint.BigInteger
import com.conradkramer.wallet.encoding.encodeHex
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder

internal class QuantitySerializer : HexademicalValueSerializer<Quantity>() {
    override fun deserialize(decoder: Decoder) = Quantity.fromString(decoder.decodeString())
}

@Serializable(QuantitySerializer::class)
class Quantity(val value: BigInteger) : HexadecimalValue() {

    constructor(data: ByteArray) : this(BigInteger(data))

    override val data: ByteArray
        get() = value.data

    fun toLong(): Long = value.toLong()
    fun toULong(): ULong = value.toULong()
    fun toInt(): Int = toLong().toInt()
    fun toBool(): Boolean = toULong() > 0UL

    override fun toString() = "0x${data.encodeHex(false)}"

    companion object {
        private val regex = "^0x[0-9a-f]*+$".toRegex(option = RegexOption.IGNORE_CASE)

        fun fromString(string: String) = fromString(string, true, regex, ::Quantity)
    }
}
