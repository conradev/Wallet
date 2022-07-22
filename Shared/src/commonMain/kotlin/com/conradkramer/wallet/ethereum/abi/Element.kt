@file:OptIn(ExperimentalSerializationApi::class)

package com.conradkramer.wallet.ethereum.abi

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@JsonClassDiscriminator("type")
internal abstract class Element

@Serializable
internal enum class StateMutability {
    @SerialName("pure")
    PURE,

    @SerialName("view")
    VIEW,

    @SerialName("nonpayable")
    NONPAYABLE,

    @SerialName("payable")
    PAYABLE
}

@Serializable
@SerialName("constructor")
internal data class Constructor(
    val inputs: List<String>?,
    val stateMutability: StateMutability
) : Element()

@Serializable
@SerialName("fallback")
internal data class Fallback(
    val stateMutability: StateMutability
) : Element()

@SerialName("function")
internal data class Function(
    val name: String,
    val inputs: List<Parameter>?,
    val outputs: List<Parameter>?,
    val stateMutability: StateMutability
) : Element() {
    @Serializable
    data class Parameter(
        val name: String,
        val type: Type
    )
}

@SerialName("receive")
internal data class Receive(
    val stateMutability: StateMutability
) : Element()

@SerialName("event")
internal data class Event(
    val name: String,
    val inputs: List<Parameter>,
    val anonymous: Boolean
) : Element() {
    @Serializable
    data class Parameter(
        val name: String,
        val type: Type,
        val indexed: Boolean
    )
}
