package com.conradkramer.wallet.encoding

@OptIn(ExperimentalUnsignedTypes::class)
internal fun ByteArray.encodeHex(allowPadding: Boolean = true): String {
    val string = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
    return if (allowPadding) { string } else { string.dropWhile { it == '0' } }
}

fun String.decodeHex(allowNibbles: Boolean = false): ByteArray {
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
