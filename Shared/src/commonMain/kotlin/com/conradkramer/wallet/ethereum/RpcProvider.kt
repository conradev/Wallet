package com.conradkramer.wallet.ethereum

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.appendPathSegments

internal interface RpcProvider {
    val endpointUrl: Url
}

internal data class AlchemyProvider(val apiKey: String) : RpcProvider {
    override val endpointUrl = URLBuilder("https://eth-mainnet.alchemyapi.io/v2")
        .appendPathSegments(apiKey)
        .build()
}

internal data class InfuraProvider(val projectId: String) : RpcProvider {
    override val endpointUrl = URLBuilder("https://mainnet.infura.io/v3")
        .appendPathSegments(projectId)
        .build()
}
