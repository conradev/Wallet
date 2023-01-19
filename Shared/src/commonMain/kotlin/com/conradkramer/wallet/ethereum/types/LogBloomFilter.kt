package com.conradkramer.wallet.ethereum.types

import com.conradkramer.wallet.crypto.Keccak256Digest
import com.conradkramer.wallet.encoding.toUInt
import io.ktor.utils.io.core.ByteOrder
import kotlin.experimental.and
import kotlin.experimental.or

class LogBloomFilter(val data: ByteArray) {

    constructor() : this(ByteArray(256))

    init {
        if (data.size != 256) throw Exception("${data.size} is an invalid size for log bloom filter")
    }

    fun insert(item: ByteArray) {
        val hash = Keccak256Digest.digest(item)
        hash.asSequence()
            .chunked(2)
            .take(3)
            .map { it.toByteArray().toUInt(ByteOrder.BIG_ENDIAN) % 2048u }
            .forEach { bit ->
                val byte = 255 - (bit.toInt() / 8)
                val shift = bit.toInt() % 8
                data[byte] = data[byte] or (1 shl shift).toByte()
            }
    }

    fun contains(data: ByteArray) = contains(LogBloomFilter().also { it.insert(data) })

    fun contains(filter: LogBloomFilter) = data
        .zip(filter.data)
        .map { (it.first and it.second) == it.second }
        .reduce { first, second -> first and second }
}
