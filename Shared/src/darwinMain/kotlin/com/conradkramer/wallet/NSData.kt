package com.conradkramer.wallet

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.Foundation.getBytes

@OptIn(ExperimentalUnsignedTypes::class)
internal fun NSData.toByteArray(): ByteArray {
    val data = ByteArray(length.convert())
    data.asUByteArray().usePinned { pinnedData ->
        getBytes(pinnedData.addressOf(0), data.size.toULong())
    }
    return data
}

@OptIn(ExperimentalUnsignedTypes::class)
internal fun ByteArray.toNSData(): NSData {
    return this.asUByteArray().usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), size.convert())
    }
}
