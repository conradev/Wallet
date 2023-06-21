@file:JvmName("ECKeyAndroid")

package com.conradkramer.wallet.crypto

import com.conradkramer.wallet.bigint.toUnsignedByteArray
import com.conradkramer.wallet.bigint.wrap
import org.bouncycastle.asn1.x9.X9IntegerConverter
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
import org.bouncycastle.math.ec.ECAlgorithms
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve
import java.math.BigInteger
import kotlin.experimental.and

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
            BouncyCastleProvider.CONFIGURATION,
        ),
    )

    actual constructor (encoded: ByteArray) : this(BigInteger(1, encoded)) {
        if (encoded.size != 32) {
            throw Exception("Invalid key length (${encoded.size})")
        }
    }

    actual val encoded: ByteArray
        get() = inner.d.toUnsignedByteArray()

    actual val publicKey: PublicKey
        get() {
            return PublicKey(curveSpec.g.multiply(inner.d))
        }

    actual operator fun plus(increment: PrivateKey): PrivateKey {
        return PrivateKey((inner.d + increment.inner.d).mod(curveSpec.n))
    }

    actual fun sign(data: ByteArray): Signature {
        val signer = ECDSASigner()
            .also { it.init(true, ECPrivateKeyParameters(inner.d, curveSpec.domainParameters)) }
        val signature = signer.generateSignature(Keccak256Digest.digest(data))

        val r = signature[0]
        val order = curveSpec.n

        // Normalize the signature into lower-S form
        var s = signature[1]
        if (s > order shr 1) {
            s = order - s
        }

        val v = (0..1)
            .first { this.publicKey == PublicKey.recover(data, r, s, it.toByte()) }
            .toByte()

        return Signature(r.wrap(), s.wrap(), v)
    }

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrivateKey

        if (inner != other.inner) return false

        return true
    }

    actual override fun hashCode(): Int {
        return inner.hashCode()
    }
}

actual class PublicKey(private val inner: BCECPublicKey) : ECKey() {
    internal constructor(q: ECPoint) : this(
        BCECPublicKey(
            "ECDSA",
            ECPublicKeySpec(q, curveSpec),
            BouncyCastleProvider.CONFIGURATION,
        ),
    )

    actual constructor(data: ByteArray) : this(curveSpec.curve.decodePoint(data))

    actual fun encoded(compressed: Boolean): ByteArray {
        return inner.q.getEncoded(compressed)
    }

    internal actual fun verify(data: ByteArray, signature: Signature): Boolean {
        return verify(data, signature.r.inner, signature.s.inner)
    }

    private fun verify(data: ByteArray, r: BigInteger, s: BigInteger): Boolean {
        val signer = ECDSASigner()
            .apply { init(false, ECPublicKeyParameters(inner.q, curveSpec.domainParameters)) }
        return signer.verifySignature(Keccak256Digest.digest(data), r, s)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PublicKey

        if (inner != other.inner) return false

        return true
    }

    override fun hashCode(): Int {
        return inner.hashCode()
    }

    internal actual companion object {
        internal fun recover(data: ByteArray, r: BigInteger, s: BigInteger, v: Byte): PublicKey {
            // SEC 1: Elliptic Curve Cryptography
            // Section 4.1.6 - Public Key Recovery Operation
            // https://www.secg.org/sec1-v2.pdf
            if (r == BigInteger.ZERO) throw Exception("r is zero")
            if (s == BigInteger.ZERO) throw Exception("s is zero")
            if ((v and 3.toByte()) != v) throw Exception("Invalid value for v")

            val order = curveSpec.n
            val curve = curveSpec.curve as SecP256K1Curve
            val x = if ((v.toInt() and 2) != 0) { r + order } else { r }
            if (x >= curve.q) throw Exception("x is too large")

            val X = X9IntegerConverter().let { it.integerToBytes(x, it.getByteLength(curve)) }
            val compressed = ByteArray(1) { (2 + (v.toInt() and 1)).toByte() } + X
            val R = curve.decodePoint(compressed)

            val O = R.multiply(order)
            if (!O.isInfinity()) throw Exception("Invalid signature")

            val hash = Keccak256Digest.digest(data)
            val hashBitLength = (hash.size * 8)
            val e = BigInteger(1, hash)
                .let { if (hashBitLength > order.bitLength()) it shr (hashBitLength - order.bitLength()) else it }

            val rInv = r.modInverse(order)
            val srInv = s.multiply(rInv).mod(order)
            val erInv = e.multiply(rInv).mod(order)

            val publicKey = PublicKey(ECAlgorithms.sumOfTwoMultiplies(R, srInv, curveSpec.g.negate(), erInv))
            if (!publicKey.verify(data, r, s)) throw Exception("Invalid signature")

            return publicKey
        }

        actual fun recover(data: ByteArray, signature: Signature): PublicKey {
            return recover(data, signature.r.inner, signature.s.inner, signature.v)
        }
    }
}

private val ECParameterSpec.domainParameters: ECDomainParameters
    get() = ECDomainParameters(curve, g, n, h, seed)
