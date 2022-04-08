package com.conradkramer.wallet.crypto

import java.security.SecureRandom

actual object SecureRandom {
    actual fun nextBytes(size: Int): ByteArray {
        val data = ByteArray(size)
        SecureRandom().nextBytes(data)
        return data
    }
}
