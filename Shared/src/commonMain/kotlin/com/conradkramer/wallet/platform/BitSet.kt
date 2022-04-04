package com.conradkramer.wallet.platform

@OptIn(ExperimentalUnsignedTypes::class)
internal fun BitSet.toSeedByteArray(): ByteArray {
    val byteArray = UByteArray((size + 7) / 8) { 0u }
    for (index in (0 until size)) {
        if (!get(index)) continue

        val byte = index / 8
        byteArray[byte] = (byteArray[byte].toUInt() or (1u shl (7 - index % 8))).toUByte()
    }
    return byteArray.toByteArray()
}

/*
This class has different underlying semantics between the JVM and Kotlin Native implementations
Any use of this class should be extensively unit tested
*/
internal expect class BitSet constructor(size: Int) {
    val size: Int
    operator fun get(index: Int): Boolean
    fun set(index: Int, value: Boolean = true)
}
