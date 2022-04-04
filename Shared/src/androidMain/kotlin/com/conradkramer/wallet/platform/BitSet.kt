package com.conradkramer.wallet.platform

import java.util.BitSet

internal actual class BitSet actual constructor(actual val size: Int) {

    private val inner = BitSet(size)

    actual operator fun get(index: Int): Boolean {
        return inner.get(index)
    }

    actual fun set(index: Int, value: Boolean) {
        inner.set(index, value)
    }
}
