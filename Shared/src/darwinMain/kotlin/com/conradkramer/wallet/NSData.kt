package com.conradkramer.wallet

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSMakeRange
import platform.Foundation.dataWithBytes
import platform.Foundation.getBytes

internal fun NSData.toByteArray() = ByteArray(length.convert())
    .also { data -> data.usePinned { getBytes(it.addressOf(0), NSMakeRange(0u, length)) } }

internal fun ByteArray.toNSData() = usePinned { NSData.dataWithBytes(it.addressOf(0), size.convert()) }
