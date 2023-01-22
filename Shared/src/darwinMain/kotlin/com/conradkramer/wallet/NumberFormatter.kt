package com.conradkramer.wallet

import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.localeWithLocaleIdentifier
import platform.Foundation.numberWithDouble

internal actual class NumberFormatter private constructor(private val inner: NSNumberFormatter) {
    actual fun string(number: Double) = inner.stringFromNumber(NSNumber.numberWithDouble(number))
        ?: throw Exception("NSNumberFormatter failed to format number $number")

    actual companion object {
        actual val currency = forLocale()
        actual val testing = forLocale("en_US")

        private fun forLocale(locale: String? = null) = NSNumberFormatter()
            .also { formatter ->
                locale?.let { formatter.locale = NSLocale.localeWithLocaleIdentifier(it) }
                formatter.numberStyle = NSNumberFormatterCurrencyStyle
                formatter.currencySymbol = ""
            }
            .let(::NumberFormatter)
    }
}
