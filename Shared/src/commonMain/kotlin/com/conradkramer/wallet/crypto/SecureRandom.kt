package com.conradkramer.wallet.crypto

import com.conradkramer.wallet.encoding.toULong

expect object SecureRandom {
    fun nextBytes(size: Int): ByteArray
}

internal fun SecureRandom.nextULong(): ULong {
    return nextBytes(8).toULong()
}
