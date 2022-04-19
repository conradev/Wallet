package com.conradkramer.wallet.crypto

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.jce.spec.ECPublicKeySpec
import org.bouncycastle.math.ec.ECPoint
import java.lang.Exception
import java.lang.Integer.min
import java.math.BigInteger

abstract class ECKey {
    protected companion object {
        val curveSpec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec("secp256k1")
    }
}

internal actual class PrivateKey internal constructor(private val inner: BCECPrivateKey) : ECKey() {
    internal constructor(d: BigInteger) : this(
        BCECPrivateKey(
            "ECDSA",
            ECPrivateKeySpec(d, curveSpec),
            BouncyCastleProvider.CONFIGURATION
        )
    )

    actual constructor (data: ByteArray) : this(BigInteger(1, data)) {
        if (data.size != 32) {
            throw Exception("Invalid key length (${data.size})")
        }
    }

    actual val encoded: ByteArray
        get() {
            val data = inner.d.toByteArray()
            val zeroes = data
                .takeWhile { it == 0.toByte() }
                .count()
            return data.copyOfRange(min(data.size - 32, zeroes), data.size)
        }

    actual val publicKey: PublicKey
        get() {
            return PublicKey(curveSpec.g.multiply(inner.d))
        }

    actual operator fun plus(increment: PrivateKey): PrivateKey {
        return PrivateKey((inner.d + increment.inner.d).mod(curveSpec.n))
    }
}

actual class PublicKey(private val inner: BCECPublicKey) : ECKey() {
    internal constructor(q: ECPoint) : this(
        BCECPublicKey(
            "ECDSA",
            ECPublicKeySpec(q, curveSpec),
            BouncyCastleProvider.CONFIGURATION
        )
    )

    actual constructor(data: ByteArray) : this(curveSpec.curve.decodePoint(data))

    actual fun encoded(compressed: Boolean): ByteArray {
        return inner.q.getEncoded(compressed)
    }
}
