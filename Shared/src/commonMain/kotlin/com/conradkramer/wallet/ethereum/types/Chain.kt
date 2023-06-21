package com.conradkramer.wallet.ethereum.types

import com.conradkramer.wallet.bigint.BigInteger
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal class ChainSerializer : KSerializer<Chain> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Chain", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = Chain(Quantity.fromString(decoder.decodeString()).toLong())
    override fun serialize(encoder: Encoder, value: Chain) = encoder.encodeString(
        Quantity(BigInteger.valueOf(value.id)).toString(),
    )
}

@Serializable(ChainSerializer::class)
enum class Chain(val id: Long) {
    MAINNET(1),
    ROPSTEN(3),
    RINKEBY(4),
    GOERLI(5),
    KOTTI(6),
    KOVAN(42),
    CLASSIC(61),
    MORDOR(63),
    ASTOR(212),
    DEV(2018),
    SEPOLIA(11155111),
    ;

    val lowercaseName = name.lowercase()

    companion object {
        private val mapping = values().associate { it.id to it }

        operator fun invoke(id: Long): Chain {
            return mapping[id] ?: throw Exception("Unrecognized chain identifier $id")
        }
    }
}
