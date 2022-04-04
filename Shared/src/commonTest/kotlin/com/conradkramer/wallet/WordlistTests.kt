package com.conradkramer.wallet

import kotlin.test.Test
import kotlin.test.assertEquals

class WordlistTests {
    @Test
    fun testWordlistLength() {
        assertEquals(2048, Wordlist.english.words.size)
    }
}
