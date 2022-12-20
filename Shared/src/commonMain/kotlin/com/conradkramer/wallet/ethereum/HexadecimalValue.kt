package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.encoding.RLP
import com.conradkramer.wallet.encoding.RLPRepresentable
import com.conradkramer.wallet.encoding.decodeHex
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder

internal abstract class HexademicalValueSerializer<T : HexadecimalValue> : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HexademicalValue", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(value.toString())
    }
}

abstract class HexadecimalValue : RLPRepresentable {
    abstract val data: ByteArray
    abstract override fun toString(): String

    override val rlp: RLP.Item
        get() = RLP.Item.Data(data)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as HexadecimalValue

        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }

    companion object {
        inline fun <reified T> fromString(
            string: String,
            allowNibbles: Boolean,
            regex: Regex,
            constructor: (ByteArray) -> T
        ): T {
            if (!regex.matches(string)) {
                throw Exception("\"$string\" is not the correct format for ${T::class.simpleName}")
            }

            return string
                .removePrefix("0x")
                .decodeHex(allowNibbles)
                .let(constructor)
        }
    }
}
