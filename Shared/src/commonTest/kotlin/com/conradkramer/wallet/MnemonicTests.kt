package com.conradkramer.wallet

import com.conradkramer.wallet.Mnemonic.Length
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class MnemonicTests {
    @Test
    fun testValidPhrasesOfAllLengths() {
        Mnemonic("kingdom rug play celery same account wish critic erase maze museum helmet")
        Mnemonic("want entire resist view gloom pencil dignity remind cement liberty chunk unique dinosaur vibrant gun")
        Mnemonic("scheme jungle fossil addict quality garage detail discover minor artefact armed space brush carry guess street surround ask")
        Mnemonic("surround illegal art hockey young ramp legal win woman budget cheap cross session ski feed under inquiry ball excite salute climb")
        Mnemonic("behave weird crash pet memory arena goat float hybrid floor grass lazy velvet volume dog solid buffalo tag record session middle practice moment pool")
    }

    @Test
    fun testInvalidPhraseLength() {
        assertFails {
            Mnemonic("behave weird crash pet memory arena goat float hybrid floor grass lazy velvet volume dog solid buffalo tag record session middle practice moment")
        }
    }

    @Test
    fun testInvalidChecksumPhrase() {
        assertFails {
            Mnemonic("behave weird crash pet memory arena goat float hybrid floor grass lazy velvet volume dog solid buffalo tag record session middle practice moment garage")
        }
    }

    @Test
    fun testUnrecognizedWord() {
        assertFails {
            Mnemonic("behave weird crash pet cudgel arena goat float hybrid floor grass lazy velvet volume dog solid buffalo tag record session middle practice moment pool")
        }
    }

    @Test
    // https://iancoleman.io/bip39/
    fun testSeedGeneration() {
        val mnemonic = Mnemonic("sniff still chief cart inside cricket embark cheese mask report gadget pen first upper ocean final traffic melt aunt peanut pledge stool stove genre")
        assertEquals("c44417c7a146ba0ea0764a3d7b1c103e1ca45fc2e367c925858471f7da0cb584c66047c8af789de0122b526a91b801796b8f7399ea3569b2b7a70667ea383ad9", mnemonic.seed().encodeHex())
    }

    @Test
    fun testGeneratingMnemonics() {
        val twelve = Mnemonic(Length.TWELVE)
        val fifteen = Mnemonic(Length.FIFTEEN)
        val eighteen = Mnemonic(Length.EIGHTEEN)
        val twentyOne = Mnemonic(Length.TWENTY_ONE)
        val twentyFour = Mnemonic(Length.TWENTY_FOUR)

        assertEquals(12, twelve.phrase.split(" ").size)
        assertEquals(15, fifteen.phrase.split(" ").size)
        assertEquals(18, eighteen.phrase.split(" ").size)
        assertEquals(21, twentyOne.phrase.split(" ").size)
        assertEquals(24, twentyFour.phrase.split(" ").size)
    }
}
