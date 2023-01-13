package com.conradkramer.wallet.viewmodel

import com.conradkramer.wallet.AccountStore
import com.conradkramer.wallet.Mnemonic
import org.koin.core.annotation.Factory

@Factory
class ImportViewModel internal constructor(private val accountStore: AccountStore) {

    val title = "Import Recovery Phrase"
    val placeholder = Mnemonic().phrase
    val action = "Import"

    fun validate(phrase: String): Boolean {
        return try {
            Mnemonic(phrase)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun accept(character: Char): Boolean {
        return (character.isLetter() || character.isWhitespace()) && character != '\n' && character != '\r'
    }

    fun clean(phrase: String): String {
        return phrase.split("[\\n\\r\\s]+".toRegex()).joinToString(" ")
    }

    fun import(phrase: String) {
        if (validate(phrase)) {
            accountStore.add(Mnemonic(phrase))
        }
    }
}
