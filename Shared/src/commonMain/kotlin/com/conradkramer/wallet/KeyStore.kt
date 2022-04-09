package com.conradkramer.wallet

import org.koin.core.module.Module

internal expect class Authentication

internal expect class KeyStore {
    val canStore: Boolean

    val all: List<String>

    fun add(data: ByteArray): String
    fun delete(id: String)
    fun use(authentication: Authentication, id: String, use: (data: ByteArray) -> Unit)
}

internal expect fun keyStoreModule(): Module
