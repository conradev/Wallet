package com.conradkramer.wallet.ethereum.abi

import com.conradkramer.wallet.crypto.Keccak256Digest
import io.ktor.utils.io.core.toByteArray

internal data class Invocation(
    private val name: String
) {
    private val types: MutableList<Type> = mutableListOf()
    private val arguments: MutableList<ByteArray> = mutableListOf()

    fun <T : Type> parameter(type: T, encode: T.() -> ByteArray): Invocation {
        types.add(type)
        arguments.add(encode(type))
        return this
    }

    private fun selector() = Keccak256Digest.digest("${name}${Type.Tuple(types)}".toByteArray())
        .copyOfRange(0, 4)

    fun build() = selector() + arguments.reduce(ByteArray::plus)
}
