package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.bigint.BigInteger
import com.conradkramer.wallet.crypto.PrivateKey
import com.conradkramer.wallet.crypto.PublicKey
import com.conradkramer.wallet.crypto.Signature
import com.conradkramer.wallet.encoding.RLP
import com.conradkramer.wallet.encoding.RLPRepresentable

internal sealed class Envelope {
    abstract val type: Byte
    abstract val unsigned: ByteArray
    abstract fun signed(signature: Signature): ByteArray

    companion object {
        fun verify(data: ByteArray): Pair<Envelope, PublicKey> {
            return when (data[0]) {
                Envelope1559.type -> RLPEnvelope.verify(data)
                else -> throw Exception("Unrecognized transaction type")
            }
        }
    }
}

internal fun PrivateKey.signEnvelope(envelope: Envelope): ByteArray {
    val type = ByteArray(1) { envelope.type }
    val input = type + envelope.unsigned
    return type + envelope.signed(sign(input))
}

internal abstract class RLPEnvelope : Envelope(), RLPRepresentable {
    override val unsigned: ByteArray
        get() = RLP.encode(rlp)

    override fun signed(signature: Signature) = RLP.encode(
        RLP.Item.List(
            rlp.values + listOf(
                RLP.Item.Data(ByteArray(1) { signature.v }),
                signature.r.rlp,
                signature.s.rlp
            )
        )
    )

    companion object {
        fun verify(data: ByteArray): Pair<Envelope, PublicKey> {
            val type = data[0]
            val item = data.sliceArray(1 until data.size)
                .let { RLP.decode(it) }
            val envelope = when (type) {
                Envelope1559.type -> Envelope1559(item)
                else -> throw Exception("Unrecognized RLP transaction type")
            }
            val input = ByteArray(1) { type } + envelope.unsigned
            val signature = signature(item)
            val publicKey = PublicKey.recover(input, signature)
            return envelope to publicKey
        }

        fun signature(item: RLP.Item): Signature {
            val signature = item.values
                .let { it.slice(it.size - 3 until it.size) }
            val v = signature[0].data[0]
            val r = BigInteger(signature[1].data)
            val s = BigInteger(signature[2].data)
            return Signature(r, s, v)
        }
    }
}

internal data class Envelope1559(
    val chainId: BigInteger,
    val nonce: BigInteger,
    val maxPriorityFeePerGas: BigInteger,
    val maxFeePerGas: BigInteger,
    val gasLimit: BigInteger,
    val destination: Address,
    val amount: BigInteger,
    val data: Data = Data(),
    val accessList: AccessList = AccessList()
) : RLPEnvelope() {
    constructor(base: RLP.Item) : this(
        BigInteger(base.values[0].data),
        BigInteger(base.values[1].data),
        BigInteger(base.values[2].data),
        BigInteger(base.values[3].data),
        BigInteger(base.values[4].data),
        Address(base.values[5].data),
        BigInteger(base.values[6].data),
        Data(base.values[7].data),
        AccessList(base.values[8])
    ) {
        val fields = base.values.size
        if (fields != 12) throw Exception("${this::class} is expecting 12 fields, not $fields")
    }

    constructor(data: ByteArray) : this(RLP.decode(data))

    override val type = Companion.type

    override val rlp: RLP.Item
        get() = RLP.Item.List(
            listOf(
                chainId.rlp,
                nonce.rlp,
                maxPriorityFeePerGas.rlp,
                maxFeePerGas.rlp,
                gasLimit.rlp,
                destination.rlp,
                amount.rlp,
                data.rlp,
                accessList.rlp
            )
        )

    companion object {
        const val type = 0x2.toByte()
    }
}

internal data class AccessList(
    val value: Map<Address, List<Data>> = emptyMap()
) : RLPRepresentable {
    constructor(rlp: RLP.Item) : this(decode(rlp))

    override val rlp: RLP.Item
        get() = RLP.Item.List(
            value.map { entry ->
                RLP.Item.List(
                    listOf(
                        entry.key.rlp,
                        RLP.Item.List(entry.value.map(Data::rlp))
                    )
                )
            }
        )

    companion object {
        private fun decode(rlp: RLP.Item): Map<Address, List<Data>> {
            return rlp.values.associate { entry ->
                (Address(entry.values[0].data) to entry.values[1].values.map { Data(it.data) })
            }
        }
    }
}
