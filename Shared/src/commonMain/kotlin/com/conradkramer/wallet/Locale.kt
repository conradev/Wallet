package com.conradkramer.wallet

expect class Locale {
    val currencyCode: Currency.Code?

    fun currency(code: Currency.Code): Currency?

    companion object {
        val current: Locale
        val testing: Locale
    }
}
