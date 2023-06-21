package com.conradkramer.wallet.ethereum.abi

internal data class Invocation(
    private val name: String,
) {
    private val types: MutableList<Type> = mutableListOf()
    private val arguments: MutableList<ByteArray> = mutableListOf()

    fun <T : Type> parameter(type: T, encode: T.() -> ByteArray): Invocation {
        types.add(type)
        arguments.add(encode(type))
        return this
    }

    fun build(): ByteArray {
        val selector = Selector(name, *types.toTypedArray())
        val arguments = if (arguments.isNotEmpty()) arguments.reduce(ByteArray::plus) else ByteArray(0)
        return selector.prefix + arguments
    }
}
