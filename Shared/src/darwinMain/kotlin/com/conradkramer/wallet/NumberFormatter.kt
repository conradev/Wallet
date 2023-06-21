package com.conradkramer.wallet

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.numberWithDouble

internal actual class NumberFormatter private constructor(
    private val inner: NSNumberFormatter,
    private val suffix: String,
) {
    actual fun string(number: Double): String = inner.stringFromNumber(NSNumber.numberWithDouble(number)) + suffix

    actual companion object {
        actual fun fiat(currency: Currency, locale: Locale?) = formatter(locale) {
            numberStyle = NSNumberFormatterCurrencyStyle
            if (currency.symbol != null) currencySymbol = currency.symbol
            currencyCode = currency.code.code
        }

        actual fun cryptocurrency(currency: Currency, locale: Locale?) = formatter(locale, " ${currency.code.code}") {
            numberStyle = NSNumberFormatterCurrencyStyle
            minimumIntegerDigits = 1U
            minimumFractionDigits = 2U
            maximumFractionDigits = 3U
            currencySymbol = ""
        }

        private fun formatter(
            locale: Locale? = null,
            suffix: String = "",
            configure: (NSNumberFormatter.() -> Unit)? = null,
        ) = NSNumberFormatter()
            .apply { setLocale(locale?.locale) }
            .apply { if (configure != null) apply(configure) }
            .let { NumberFormatter(it, suffix) }
    }
}
