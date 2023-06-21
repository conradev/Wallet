package com.conradkramer.wallet

internal data class BiometricPromptInfo(
    val title: String,
    val subtitle: String,
    val reason: String,
    val cancelTitle: String,
)

expect class BiometricPromptHost

internal interface BiometricAuthenticator<Context> {
    fun context(id: String): Context
    suspend fun prompt(context: Context, host: BiometricPromptHost?, info: BiometricPromptInfo): Boolean
    fun <R> decrypt(context: Context, data: ByteArray, handler: (data: ByteArray) -> R): R
}

internal interface KeyStore<Context> : BiometricAuthenticator<Context> {
    val canStore: Boolean

    val all: Set<String>
    fun generate(id: String)
    fun delete(id: String)
    fun reset()

    fun encrypt(id: String, data: ByteArray): ByteArray
}

expect class AuthenticationContext(id: String) {
    val id: String
}

internal expect class HardwareKeyStore : KeyStore<AuthenticationContext>
