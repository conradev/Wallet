package com.conradkramer.wallet

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

internal actual class NumberFormatter private constructor(private val inner: NumberFormat) {
    actual fun string(number: Double): String {
        return inner.format(number)
    }

    actual companion object {
        actual val currency = forLocale()
        actual val testing = forLocale(Locale("en", "US"))

        private fun forLocale(locale: Locale? = null): NumberFormatter {
            val format = locale?.let { NumberFormat.getCurrencyInstance(it) } ?: NumberFormat.getCurrencyInstance()
            (format as DecimalFormat).decimalFormatSymbols = DecimalFormatSymbols().also { it.currencySymbol = "" }
            return NumberFormatter(format)
        }
    }
}
