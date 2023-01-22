package com.conradkramer.wallet.crypto

import com.conradkramer.wallet.autorelease
import com.conradkramer.wallet.toKotlinString
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import platform.Security.SecCopyErrorMessageString
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess

actual object SecureRandom {
    actual fun nextBytes(size: Int) = ByteArray(size)
        .also { SecRandomCopyBytes(null, it.size.convert(), it.refTo(0)).errSec() }
}

private fun Int.errSec() {
    if (this@errSec == errSecSuccess) return
    val message = memScoped {
        SecCopyErrorMessageString(this@errSec, null)
            ?.autorelease(this)
            ?.toKotlinString()
    }
    throw Exception(message ?: "An unknown error ocurred ${this@errSec}")
}
