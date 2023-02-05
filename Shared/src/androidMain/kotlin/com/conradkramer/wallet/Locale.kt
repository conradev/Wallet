package com.conradkramer.wallet

import android.os.LocaleList

actual class Locale(val locale: java.util.Locale) {
    actual val currencyCode: Currency.Code?
        get() = java.util.Currency.getInstance(locale)?.currencyCode?.let { Currency.Code(it) }

    actual fun currency(code: Currency.Code): Currency? {
        val currency = java.util.Currency.getInstance(code.code) ?: return null
        return Currency(
            Currency.Code(currency.currencyCode),
            currency.getDisplayName(locale),
            currency.getSymbol(locale)
        )
    }

    actual companion object {
        actual val current: Locale
            get() = Locale(LocaleList.getDefault()[0])

        actual val testing: Locale
            get() = Locale(java.util.Locale("en", "US"))
    }
}
