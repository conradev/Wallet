package com.conradkramer.wallet

import com.conradkramer.wallet.encoding.encodeHex
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal fun assertDataEquals(expected: ByteArray, actual: ByteArray) = assertEquals(
    expected.encodeHex(),
    actual.encodeHex(),
)

internal fun assertDataNotEquals(expected: ByteArray, actual: ByteArray) = assertNotEquals(
    expected.encodeHex(),
    actual.encodeHex(),
)
