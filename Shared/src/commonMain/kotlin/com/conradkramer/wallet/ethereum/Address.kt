package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.crypto.Keccak256Digest
import com.conradkramer.wallet.encoding.encodeHex
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder

internal class AddressSerializer : HexademicalValueSerializer<Address>() {
    override fun deserialize(decoder: Decoder) = Address.fromString(decoder.decodeString())
}

@Serializable(with = AddressSerializer::class)
class Address(override val data: ByteArray) : HexadecimalValue() {
    init {
        if (data.size != 20) throw Exception("Address is incorrect length")
    }

    override fun toString(): String {
        val string = data.encodeHex()
        val hash = Keccak256Digest.digest(string.toByteArray()).encodeHex()
        val checksummed = CharArray(string.length) { index ->
            val uppercase = string[index].isLetter() && hash[index].toString().toInt(16) > 7
            return@CharArray if (uppercase) string[index].uppercase().first() else string[index]
        }
        return "0x${checksummed.concatToString()}"
    }

    private val lowercased: String
        get() = "0x${data.encodeHex()}"

    private val uppercased: String
        get() = "0x${data.encodeHex().uppercase()}"

    companion object {
        private val regex = "^0x[0-9a-f]{40}$".toRegex(option = RegexOption.IGNORE_CASE)

        fun fromString(string: String): Address {
            val address = fromString(string, false, regex, ::Address)
            if (address.toString() != string && address.lowercased != string && address.uppercased != string) {
                throw Exception("Ethereum address has invalid checksum")
            }
            return address
        }
    }
}
