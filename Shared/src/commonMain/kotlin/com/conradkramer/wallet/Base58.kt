package com.conradkramer.wallet

import com.conradkramer.wallet.platform.SHA256Digest

private const val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"

// https://datatracker.ietf.org/doc/html/draft-msporny-base58-03
internal fun ByteArray.encodeBase58(): String {
    var zeroes = 0
    for (index in 0 until size) {
        if (this[index] == 0.toByte()) {
            zeroes++
        } else {
            break
        }
    }

    val capacity = (size - zeroes) * 138 / 100 + 1
    val output = ByteArray(capacity)
    var end = capacity - 1

    for (index in zeroes until size) {
        var carry = this[index].toUByte().toUInt()

        var outputIndex = capacity - 1
        while (outputIndex > end || carry != 0u) {
            carry += output[outputIndex].toUInt() * 256u
            output[outputIndex] = (carry % 58u).toByte()
            carry /= 58u
            outputIndex--
        }
        end = outputIndex
    }

    return CharArray(zeroes + (capacity - 1 - end)) {
        if (it < zeroes) {
            alphabet[0]
        } else {
            alphabet[output[end + 1 + it - zeroes].toInt()]
        }
    }.concatToString()
}

internal fun ByteArray.encodeBase58Check(): String {
    val checksum = SHA256Digest().digest(SHA256Digest().digest(this)).copyOf(4)
    return (this + checksum).encodeBase58()
}
