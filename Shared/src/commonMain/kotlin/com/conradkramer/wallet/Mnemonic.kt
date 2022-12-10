package com.conradkramer.wallet

import com.conradkramer.wallet.crypto.BitSet
import com.conradkramer.wallet.crypto.PBKDF2SHA512Derivation
import com.conradkramer.wallet.crypto.SHA256Digest
import com.conradkramer.wallet.crypto.toSeedByteArray
import com.conradkramer.wallet.encoding.toUInt
import io.ktor.utils.io.core.ByteOrder
import io.ktor.utils.io.core.toByteArray
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

internal class Mnemonic
@Throws(Exception::class)
constructor(phrase: String) {
    val phrase: String
    val length: Length

    constructor(length: Length) : this(generate(length))
    constructor() : this(Length.TWENTY_FOUR)

    init {
        val words = phrase
            .split("\\s+".toRegex())
            .filter(String::isNotEmpty)

        val length = try {
            Length.values().first { it.value == words.size }
        } catch (e: NoSuchElementException) {
            throw Exception("Invalid mnemonic length (${words.size})")
        }

        val indices = words.map {
            when (val index = Wordlist.english.words.indexOf(it)) {
                -1 -> throw Exception("Invalid word $it")
                else -> index.toUInt()
            }
        }

        val bits = BitSet(length.value * BITS_PER_WORD)
        for ((position, index) in indices.withIndex()) {
            val offset = position * BITS_PER_WORD
            for (bit in 0 until BITS_PER_WORD) {
                bits.set(offset + bit, ((index shr (BITS_PER_WORD - bit - 1)) and 1u) == 1u)
            }
        }

        val dataWithChecksum = bits.toSeedByteArray()
        val data = dataWithChecksum.copyOfRange(0, dataWithChecksum.size - 1)
        val checksum = (dataWithChecksum.last().toUInt() shr (BITS_PER_BYTE - length.checksumBits)).toUByte()
        val computedChecksum = (SHA256Digest.digest(data).first().toUInt() shr (BITS_PER_BYTE - length.checksumBits))
            .toUByte()
        if (checksum != computedChecksum) {
            throw Exception("Provided checksum $checksum does not match computed checksum $computedChecksum")
        }

        this.phrase = words.joinToString(" ")
        this.length = length
    }

    fun seed(passphrase: String = ""): ByteArray {
        val salt = ("mnemonic$passphrase").toByteArray()
        return PBKDF2SHA512Derivation.compute(salt, phrase, 2048)
    }

    enum class Length(val value: Int) {
        TWELVE(12),
        FIFTEEN(15),
        EIGHTEEN(18),
        TWENTY_ONE(21),
        TWENTY_FOUR(24);

        val bits: Int
            get() {
                return when (this) {
                    TWELVE -> 128
                    FIFTEEN -> 160
                    EIGHTEEN -> 192
                    TWENTY_ONE -> 224
                    TWENTY_FOUR -> 256
                }
            }

        val checksumBits: Int
            get() {
                return bits / 32
            }
    }

    private companion object {
        const val BITS_PER_BYTE = 8
        const val BITS_PER_WORD = 11
        val WORD_BITMASK = (2.0.pow(BITS_PER_WORD) - 1).toUInt()

        fun generate(length: Length): String {
            var data = Random.Default.nextBytes(length.bits / BITS_PER_BYTE)
            data += SHA256Digest.digest(data).first()
            return (0 until length.value)
                .map { index(data, it) }
                .joinToString(" ") { Wordlist.english.words[it] }
        }

        fun index(data: ByteArray, offset: Int): Int {
            val startBit = offset * BITS_PER_WORD
            val byteReadOffset = min(startBit / BITS_PER_BYTE, data.size - UInt.SIZE_BYTES)
            val ignoredBits = startBit - (byteReadOffset * BITS_PER_BYTE)
            val rightShift = UInt.SIZE_BITS - ignoredBits - BITS_PER_WORD
            return data
                .sliceArray(byteReadOffset until byteReadOffset + UInt.SIZE_BYTES)
                .toUInt(ByteOrder.BIG_ENDIAN)
                .shr(rightShift)
                .and(WORD_BITMASK)
                .toInt()
        }
    }
}
