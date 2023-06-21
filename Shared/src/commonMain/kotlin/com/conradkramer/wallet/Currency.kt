package com.conradkramer.wallet

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal class CurrencyCodeSerializer : KSerializer<Currency.Code> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Currency.Code", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Currency.Code) = encoder.encodeString(value.code)
    override fun deserialize(decoder: Decoder): Currency.Code = Currency.Code(decoder.decodeString())
}

data class Currency(
    val code: Code,
    val name: String,
    val symbol: String?,
    val decimals: Int = 2,
) {
    @Serializable(CurrencyCodeSerializer::class)
    data class Code(val code: String) {
        override fun toString() = code

        companion object {
            val ETH = Code("ETH")
            val USD = Code("USD")
        }
    }

    companion object {
        val ETH: Currency
            get() = Currency(Code.ETH, "Ethereum", "Ξ", 18)
        val USD: Currency
            get() = Currency(Code.USD, "US Dollars", "$", 2)
    }
}
