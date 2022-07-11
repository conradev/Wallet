package com.conradkramer.wallet

enum class Coin(val number: Int) {
    BITCOIN(0),
    ETHEREUM(60);

    companion object {
        private val mapping = values().associate { it.number to it }

        operator fun invoke(number: Int): Coin {
            return mapping[number] ?: throw Exception("Unrecognized coin identifier $number")
        }
    }
}
