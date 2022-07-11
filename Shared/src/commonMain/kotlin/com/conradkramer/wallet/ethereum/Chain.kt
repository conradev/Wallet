package com.conradkramer.wallet.ethereum

enum class Chain(val id: Long) {
    MAINNET(1),
    ROPSTEN(3),
    RINKEBY(4),
    GOERLI(5),
    KOTTI(6),
    KOVAN(42),
    CLASSIC(61),
    MORDOR(63),
    ASTOR(212),
    DEV(2018);

    val lowercaseName = name.lowercase()

    companion object {
        private val mapping = values().associate { it.id to it }

        operator fun invoke(id: Long): Chain {
            return mapping[id] ?: throw Exception("Unrecognized chain identifier $id")
        }
    }
}
