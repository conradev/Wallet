package com.conradkramer.wallet

internal expect class NumberFormatter {
    fun string(number: Double): String

    companion object {
        val currency: NumberFormatter
        val testing: NumberFormatter
    }
}
