package com.conradkramer.wallet

actual sealed class Platform {
    actual companion object {
        actual val isSimulator = true
    }
}
