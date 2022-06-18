package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.encoding.encodeHex
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder

internal class QuantitySerializer : HexademicalValueSerializer<Quantity>() {
    override fun deserialize(decoder: Decoder): Quantity {
        return Quantity.fromString(decoder.decodeString())
    }
}

@Serializable(with = QuantitySerializer::class)
internal class Quantity(val value: BigInteger) : HexadecimalValue() {

    constructor(data: ByteArray) : this(BigInteger(data))

    override val data: ByteArray
        get() = value.data

    override fun toString(): String {
        return "0x${data.encodeHex(false)}"
    }

    companion object {
        private val regex = "^0x[0-9a-f]*$".toRegex(option = RegexOption.IGNORE_CASE)

        fun fromString(string: String): Quantity {
            return fromString(string, true, regex, ::Quantity)
        }
    }
}
