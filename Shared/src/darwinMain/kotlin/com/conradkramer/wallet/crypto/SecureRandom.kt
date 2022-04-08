package com.conradkramer.wallet.crypto

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Security.SecRandomCopyBytes

actual object SecureRandom {
    actual fun nextBytes(size: Int): ByteArray {
        val data = ByteArray(size)
        data.usePinned { pinnedData ->
            SecRandomCopyBytes(null, data.size.convert(), pinnedData.addressOf(0))
        }
        return data
    }
}
