package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.encoding.encodeHex
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder

internal class DataSerializer : HexademicalValueSerializer<Data>() {
    override fun deserialize(decoder: Decoder): Data {
        return Data.fromString(decoder.decodeString())
    }
}

@Serializable(with = DataSerializer::class)
class Data(override val data: ByteArray) : HexadecimalValue() {

    constructor() : this(ByteArray(0))

    override fun toString(): String {
        return "0x${data.encodeHex()}"
    }

    companion object {
        private val regex = "^0x[0-9a-f]+$".toRegex(option = RegexOption.IGNORE_CASE)

        fun fromString(string: String): Data {
            return fromString(string, false, regex, ::Data)
        }
    }
}
