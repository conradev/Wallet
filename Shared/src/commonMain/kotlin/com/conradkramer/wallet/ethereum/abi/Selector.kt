package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.crypto.Keccak256Digest
import com.conradkramer.wallet.encoding.toByteArray
import com.conradkramer.wallet.encoding.toUInt
import io.ktor.utils.io.core.toByteArray

internal data class Selector(
    val name: String,
    val types: List<Type>
) {
    constructor(name: String, vararg types: Type) : this(name, types.toList())

    override fun toString() = "${name}${Type.Tuple(types)}"

    val data: ByteArray
        get() = Keccak256Digest.digest(toString().toByteArray())

    val prefix: ByteArray
        get() = data.copyOfRange(0, 4)

    val integer: UInt
        get() = prefix.toUInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Selector

        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}
