package com.conradkramer.wallet.platform

internal actual class BitSet actual constructor(size: Int) {

    private val inner = kotlin.native.BitSet(size)

    actual val size: Int
        get() = inner.size

    actual operator fun get(index: Int): Boolean {
        return inner[index]
    }

    actual fun set(index: Int, value: Boolean) {
        inner.set(index, value)
    }
}
