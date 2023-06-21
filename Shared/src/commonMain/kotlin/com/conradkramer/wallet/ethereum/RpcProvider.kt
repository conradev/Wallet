package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.ethereum.types.Chain
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.appendPathSegments

internal interface RpcProvider {
    val supportedChains: Set<Chain>

    fun endpointUrl(chain: Chain): Url
}

/**
 * Errata:
 * - Block parameters are not required and default to "current". Geth requires the block parameter.
 */
internal data class AlchemyProvider(val apiKeys: Map<Chain, String>) : RpcProvider {
    override val supportedChains = apiKeys.keys

    override fun endpointUrl(chain: Chain) = URLBuilder("https://eth-${chain.lowercaseName}.alchemyapi.io/v2")
        .appendPathSegments(apiKeys[chain] ?: throw Exception("Alchemy does not support $chain"))
        .build()
}

internal data class InfuraProvider(val projectId: String) : RpcProvider {
    override val supportedChains = setOf(
        Chain.MAINNET,
        Chain.ROPSTEN,
        Chain.KOVAN,
        Chain.RINKEBY,
        Chain.GOERLI,
    )

    override fun endpointUrl(chain: Chain) = URLBuilder("https://${chain.lowercaseName}.infura.io/v3")
        .appendPathSegments(projectId)
        .build()
}

/**
 * Errata:
 * - Negative `id` values return a "problem parsing request body" error
 */
internal class Cloudflare(
    val host: String = "web3-trial.cloudflare-eth.com",
) : RpcProvider {
    override val supportedChains = setOf(
        Chain.MAINNET,
        Chain.RINKEBY,
        Chain.GOERLI,
        Chain.SEPOLIA,
    )

    override fun endpointUrl(chain: Chain) = URLBuilder(protocol = URLProtocol.HTTPS, host = host)
        .appendPathSegments("v1", chain.lowercaseName)
        .build()
}

internal open class ChainRpcProvider(
    val url: Url,
    chain: Chain,
) : RpcProvider {
    override val supportedChains = setOf(chain)

    override fun endpointUrl(chain: Chain) = url
}
