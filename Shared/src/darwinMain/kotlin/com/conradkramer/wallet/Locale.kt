package com.conradkramer.wallet

import platform.Foundation.NSLocale
import platform.Foundation.autoupdatingCurrentLocale
import platform.Foundation.availableLocaleIdentifiers
import platform.Foundation.currencyCode
import platform.Foundation.currencySymbol
import platform.Foundation.localeWithLocaleIdentifier
import platform.Foundation.localizedStringForCurrencyCode

actual class Locale(val locale: NSLocale) {
    actual val currencyCode: Currency.Code?
        get() = locale.currencyCode?.let { Currency.Code(it) }

    actual fun currency(code: Currency.Code): Currency? {
        val name = locale.localizedStringForCurrencyCode(code.code)
        val symbol = fromCurrencyCode(code)?.currencySymbol
        return if (name != null && symbol != null) Currency(code, name, symbol) else null
    }

    private val currencySymbol: String
        get() = locale.currencySymbol

    actual companion object {
        actual val current = Locale(NSLocale.autoupdatingCurrentLocale)
        actual val testing = Locale(NSLocale.localeWithLocaleIdentifier("en_US"))

        private fun fromCurrencyCode(code: Currency.Code) = NSLocale.availableLocaleIdentifiers
            .map { NSLocale.localeWithLocaleIdentifier(it as String) }
            .find { it.currencyCode?.uppercase() == code.code.uppercase() }
            ?.let(::Locale)
    }
}
