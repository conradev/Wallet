@file:JvmName("ECKeyAndroid")
package com.conradkramer.wallet.crypto

import com.conradkramer.wallet.bigint.BigInteger
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec
import org.bouncycastle.jce.spec.ECParameterSpec
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.jce.spec.ECPublicKeySpec
import org.bouncycastle.math.ec.ECPoint
import java.lang.Integer.min

abstract class ECKey {
    protected companion object {
        val curveSpec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec("secp256k1")
    }
}

internal actual class PrivateKey internal constructor(private val inner: BCECPrivateKey) : ECKey() {
    internal constructor(d: BigInteger) : this(
        BCECPrivateKey(
            "ECDSA",
            ECPrivateKeySpec(d.inner, curveSpec),
            BouncyCastleProvider.CONFIGURATION
        )
    )

    actual constructor (data: ByteArray) : this(BigInteger(data)) {
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
        return PrivateKey(BigInteger((inner.d + increment.inner.d).mod(curveSpec.n)))
    }

    actual fun sign(data: ByteArray): Signature {
        val signer = ECDSASigner()
            .also { it.init(true, ECPrivateKeyParameters(inner.d, curveSpec.domainParameters)) }
        val signature = signer.generateSignature(Keccak256Digest.digest(data))

        val r = signature[0]

        // Normalize the signature into lower-S form
        var s = signature[1]
        if (s > curveSpec.n.shiftRight(1)) {
            s = curveSpec.n - s
        }

        val v = r.mod(2.toBigInteger()).toByte()

        return Signature(BigInteger(r), BigInteger(s), v)
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

    internal actual fun verify(data: ByteArray, signature: Signature): Boolean {
        val signer = ECDSASigner()
            .also { it.init(false, ECPublicKeyParameters(inner.q, curveSpec.domainParameters)) }
        return signer.verifySignature(Keccak256Digest.digest(data), signature.r.inner, signature.s.inner)
    }
}

private val ECParameterSpec.domainParameters: ECDomainParameters
    get() = ECDomainParameters(curve, g, n, h, seed)
