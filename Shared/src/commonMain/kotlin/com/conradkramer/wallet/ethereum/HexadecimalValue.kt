package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.decodeHex
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder

internal abstract class HexademicalValueSerializer<T : HexademicalValue> : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HexademicalValue", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(this.toString())
    }
}

internal abstract class HexademicalValue(val data: ByteArray) {
    abstract override fun toString(): String

    companion object {
        fun <T> fromString(string: String, allowNibbles: Boolean, regex: Regex, constructor: (ByteArray) -> T): T {
            if (!regex.matches(string)) {
                throw Exception("Address is incorrect format")
            }

            return string
                .removePrefix("0x")
                .decodeHex(allowNibbles)
                .let(constructor)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Address

        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}
