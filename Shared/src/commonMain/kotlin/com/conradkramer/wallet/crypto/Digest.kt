package com.conradkramer.wallet.crypto

internal expect object SHA256Digest {
    fun digest(data: ByteArray): ByteArray
}

internal expect object SHA512Mac {
    fun authenticationCode(data: ByteArray, key: ByteArray): ByteArray
}

internal expect object PBKDF2SHA512Derivation {
    fun compute(salt: ByteArray, password: String, rounds: Int): ByteArray
}

internal expect object RIPEMD160Digest {
    fun digest(data: ByteArray): ByteArray
}

internal expect object Keccak256Digest {
    fun digest(data: ByteArray): ByteArray
}
