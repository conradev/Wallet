package com.conradkramer.wallet

internal expect class Authentication
internal expect class KeyStoreContext

internal expect class KeyStore(keyStoreContext: KeyStoreContext) {
    val canStore: Boolean

    val all: List<String>

    fun add(data: ByteArray): String
    fun delete(id: String)
    fun use(authentication: Authentication, id: String, use: (data: ByteArray) -> Unit)
}
