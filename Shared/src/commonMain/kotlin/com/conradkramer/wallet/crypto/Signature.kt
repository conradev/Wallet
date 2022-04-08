package com.conradkramer.wallet.crypto

import com.conradkramer.wallet.bigint.BigInteger

internal data class Signature(
    val r: BigInteger,
    val s: BigInteger,
    val v: Byte
) {
    fun toByteArray(): ByteArray {
        return r.data + s.data + v
    }
}
