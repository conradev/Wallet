package com.conradkramer.wallet

interface Currency {
    val decimals: Int
    val displayName: String
    val symbol: String
}
