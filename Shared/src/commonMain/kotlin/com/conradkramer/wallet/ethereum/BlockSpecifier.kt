package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.bigint.BigInteger
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
internal enum class BlockTag {
    @SerialName("earliest")
    EARLIEST,

    @SerialName("latest")
    LATEST,

    @SerialName("pending")
    PENDING;

    override fun toString(): String {
        return name.lowercase()
    }

    companion object {
        fun fromString(string: String): BlockTag? {
            return values().firstOrNull { it.name == string.uppercase() }
        }
    }
}

@Serializable(with = BlockSpecifierSerializer::class)
internal sealed class BlockSpecifier {
    data class Number(val quantity: Quantity) : BlockSpecifier() {
        override fun toString(): String {
            return quantity.toString()
        }
    }
    data class Tag(val tag: BlockTag) : BlockSpecifier() {
        override fun toString(): String {
            return tag.toString()
        }
    }

    companion object {
        val EARLIEST: BlockSpecifier = Tag(BlockTag.EARLIEST)
        val LATEST: BlockSpecifier = Tag(BlockTag.LATEST)
        val PENDING: BlockSpecifier = Tag(BlockTag.PENDING)

        fun fromNumber(number: Long): BlockSpecifier {
            return Number(Quantity(BigInteger.valueOf(number)))
        }

        fun fromString(string: String): BlockSpecifier {
            return BlockTag.fromString(string)?.let(::Tag)
                ?: Number(Quantity.fromString(string))
        }
    }
}

internal class BlockSpecifierSerializer : KSerializer<BlockSpecifier> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BlockSpecifier", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BlockSpecifier) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): BlockSpecifier {
        return BlockSpecifier.fromString(decoder.decodeSerializableValue(String.serializer()))
    }
}
