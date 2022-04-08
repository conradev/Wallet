package com.conradkramer.wallet

expect sealed class Platform {
    companion object {
        val isSimulator: Boolean
    }
}
