package com.conradkramer.wallet.crypto

expect object SecureRandom {
    fun nextBytes(size: Int): ByteArray
}
