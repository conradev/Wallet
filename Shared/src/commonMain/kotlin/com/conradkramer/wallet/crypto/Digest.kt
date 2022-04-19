package com.conradkramer.wallet.crypto

internal expect class SHA256Digest() {
    fun digest(data: ByteArray): ByteArray
}

internal expect class SHA512Mac() {
    fun authenticationCode(data: ByteArray, key: ByteArray): ByteArray
}

internal expect class PBKDF2SHA512Derivation() {
    fun compute(salt: ByteArray, password: String, rounds: Int): ByteArray
}

internal expect class RIPEMD160Digest() {
    fun digest(data: ByteArray): ByteArray
}

internal expect class Keccak256Digest() {
    fun digest(data: ByteArray): ByteArray
}
