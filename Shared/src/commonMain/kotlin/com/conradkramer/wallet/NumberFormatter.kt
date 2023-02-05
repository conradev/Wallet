package com.conradkramer.wallet

internal expect class NumberFormatter {
    fun string(number: Double): String

    companion object {
        fun fiat(currency: Currency, locale: Locale? = null): NumberFormatter
        fun cryptocurrency(currency: Currency, locale: Locale? = null): NumberFormatter
    }
}
