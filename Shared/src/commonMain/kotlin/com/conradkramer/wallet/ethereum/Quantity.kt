package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.encodeHex
import com.conradkramer.wallet.platform.BigInteger
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder

internal class QuantitySerializer : HexademicalValueSerializer<Quantity>() {
    override fun deserialize(decoder: Decoder): Quantity {
        return Quantity.fromString(decoder.decodeString())
    }
}

@Serializable(with = QuantitySerializer::class)
class Quantity(data: ByteArray) : HexademicalValue(data) {

    val value: BigInteger
        get() = BigInteger(data)

    override fun toString(): String {
        return "0x${data.encodeHex(false)}"
    }

    companion object {
        private val regex = "^0x[0-9a-f]+$".toRegex(option = RegexOption.IGNORE_CASE)

        fun fromString(string: String): Quantity {
            return fromString(string, true, regex, ::Quantity)
        }
    }
}
