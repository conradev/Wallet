package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.crypto.PrivateKey
import com.conradkramer.wallet.crypto.Signature
import io.ktor.utils.io.core.toByteArray

internal fun PrivateKey.signMessage(data: ByteArray): Signature {
    return sign("\u0019Ethereum Signed Message:\n${data.size}".toByteArray() + data)
}
