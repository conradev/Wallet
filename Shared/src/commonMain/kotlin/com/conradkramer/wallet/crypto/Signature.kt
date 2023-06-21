package com.conradkramer.wallet.crypto

import com.conradkramer.wallet.bigint.BigInteger
import kotlin.experimental.and

internal data class Signature(
    val r: BigInteger,
    val s: BigInteger,
    val v: Byte,
) {
    init {
        if (r == BigInteger.valueOf(0)) throw Exception("r is zero")
        if (s == BigInteger.valueOf(0)) throw Exception("s is zero")
        if (r.data.size != 32) throw Exception("${r.data.size} is not a valid size for r")
        if (s.data.size != 32) throw Exception("${s.data.size} is not a valid size for s")
        if ((v and 3.toByte()) != v) throw Exception("$v is not a valid value for v")
    }

    fun toByteArray() = r.data + s.data + v
}
