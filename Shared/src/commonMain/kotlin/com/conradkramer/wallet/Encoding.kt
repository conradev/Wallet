package com.conradkramer.wallet

import io.ktor.utils.io.core.ByteOrder

@OptIn(ExperimentalUnsignedTypes::class)
internal fun ByteArray.encodeHex(allowPadding: Boolean = true): String {
    val string = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
    return if (allowPadding) { string } else { string.dropWhile { it == '0' } }
}

internal fun String.decodeHex(allowNibbles: Boolean = false): ByteArray {
    val string = when (length % 2 to allowNibbles) {
        (1 to true) -> "0$this"
        (1 to false) -> throw Exception("Must have an even length")
        else -> this
    }

    val byteIterator = string
        .chunkedSequence(2)
        .map { it.toInt(16).toByte() }
        .iterator()

    return ByteArray(string.length / 2) { byteIterator.next() }
}

fun UInt.toByteArray(order: ByteOrder = ByteOrder.LITTLE_ENDIAN): ByteArray {
    return ByteArray(4) {
        val offset = when (order) {
            ByteOrder.BIG_ENDIAN -> (3 - it) * 8
            ByteOrder.LITTLE_ENDIAN -> it * 8
            else -> throw IllegalArgumentException()
        }
        (this shr offset).toByte()
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.toUInt(order: ByteOrder = ByteOrder.LITTLE_ENDIAN): UInt {
    return asUByteArray().toUInt(order)
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun UByteArray.toUInt(order: ByteOrder = ByteOrder.LITTLE_ENDIAN): UInt {
    return when (order) {
        ByteOrder.BIG_ENDIAN -> this[3].toUInt() + (this[2].toUInt() shl 8) + (this[1].toUInt() shl 16) + (this[0].toUInt() shl 24)
        ByteOrder.LITTLE_ENDIAN -> this[0].toUInt() + (this[1].toUInt() shl 8) + (this[2].toUInt() shl 16) + (this[3].toUInt() shl 24)
        else -> throw IllegalArgumentException()
    }
}
