package com.conradkramer.wallet.ethereum.requests

import com.conradkramer.wallet.ethereum.types.Address
import com.conradkramer.wallet.ethereum.types.Data
import kotlinx.serialization.json.JsonElement

internal data class PersonalSign(
    override val data: Data,
    override val address: Address
) : Request(), SignRequest {
    constructor(params: List<JsonElement>) : this(
        decode(params, 0),
        decode(params, 1)
    )

    override val method = Companion.method
    override val params: List<JsonElement>
        get() = listOf(encode(address), encode(data))

    companion object {
        const val method = "personal_sign"
    }
}
