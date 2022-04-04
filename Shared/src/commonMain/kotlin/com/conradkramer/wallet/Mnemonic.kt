package com.conradkramer.wallet

import com.conradkramer.wallet.platform.BitSet
import com.conradkramer.wallet.platform.PBKDF2SHA512Derivation
import com.conradkramer.wallet.platform.SHA256Digest
import com.conradkramer.wallet.platform.toSeedByteArray
import io.ktor.utils.io.core.ByteOrder
import io.ktor.utils.io.core.toByteArray
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

internal class Mnemonic @Throws(Exception::class) constructor(phrase: String) {

    val phrase: String

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

        val bytesWithChecksum = bits.toSeedByteArray()
        val bytes = bytesWithChecksum.copyOfRange(0, bytesWithChecksum.size - 1)
        val checksum = (bytesWithChecksum.last().toUInt() shr (BITS_PER_BYTE - length.checksumBits)).toUByte()
        val computedChecksum = (SHA256Digest().digest(bytes).first().toUInt() shr (BITS_PER_BYTE - length.checksumBits)).toUByte()
        if (checksum != computedChecksum) {
            throw Exception("Provided checksum $checksum does not match computed checksum $computedChecksum")
        }

        this.phrase = words.joinToString(" ")
    }

    fun seed(passphrase: String = ""): ByteArray {
        val salt = ("mnemonic$passphrase").toByteArray()
        return PBKDF2SHA512Derivation().compute(salt, phrase, 2048)
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
            var bytes = Random.Default.nextBytes(length.bits / BITS_PER_BYTE)
            bytes += SHA256Digest().digest(bytes).first()
            return (0 until length.value)
                .map { index(bytes, it) }
                .joinToString(" ") { Wordlist.english.words[it] }
        }

        fun index(bytes: ByteArray, offset: Int): Int {
            val startBit = offset * BITS_PER_WORD
            val byteReadOffset = min(startBit / BITS_PER_BYTE, bytes.size - UInt.SIZE_BYTES)
            val ignoredBits = startBit - (byteReadOffset * BITS_PER_BYTE)
            val rightShift = UInt.SIZE_BITS - ignoredBits - BITS_PER_WORD
            return bytes
                .sliceArray(byteReadOffset until byteReadOffset + UInt.SIZE_BYTES)
                .toUInt(ByteOrder.BIG_ENDIAN)
                .shr(rightShift)
                .and(WORD_BITMASK)
                .toInt()
        }
    }
}
