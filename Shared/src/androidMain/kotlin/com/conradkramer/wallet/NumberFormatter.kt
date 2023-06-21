package com.conradkramer.wallet

import java.text.DecimalFormat
import java.text.NumberFormat

internal actual class NumberFormatter private constructor(
    private val inner: NumberFormat,
    private val suffix: String = "",
) {
    actual fun string(number: Double) = inner.format(number) + suffix

    actual companion object {
        actual fun fiat(currency: Currency, locale: Locale?): NumberFormatter {
            val format = locale?.let { DecimalFormat.getCurrencyInstance(it.locale) }
                ?: DecimalFormat.getCurrencyInstance()
            format.currency = java.util.Currency.getInstance(currency.code.code)
            return NumberFormatter(format)
        }

        actual fun cryptocurrency(currency: Currency, locale: Locale?): NumberFormatter {
            val format = locale?.let { DecimalFormat.getInstance(it.locale) } ?: DecimalFormat.getInstance()
            format.minimumIntegerDigits = 1
            format.minimumFractionDigits = 2
            format.maximumFractionDigits = 3
            return NumberFormatter(format, " ${currency.code.code}")
        }
    }
}
