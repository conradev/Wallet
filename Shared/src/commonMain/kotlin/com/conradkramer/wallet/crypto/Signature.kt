package com.conradkramer.wallet.crypto

import com.conradkramer.wallet.bigint.BigInteger
import kotlin.experimental.and

internal data class Signature(
    val r: BigInteger,
    val s: BigInteger,
    val v: Byte
) {
    init {
        if (r == BigInteger.valueOf(0)) throw Exception("r is zero")
        if (s == BigInteger.valueOf(0)) throw Exception("s is zero")
        if (r.data.size != 32) throw Exception("Invalid size for r")
        if (s.data.size != 32) throw Exception("Invalid size for s")
        if ((v and 3.toByte()) != v) throw Exception("Invalid value for v")
    }

    fun toByteArray(): ByteArray {
        return r.data + s.data + v
    }
}
