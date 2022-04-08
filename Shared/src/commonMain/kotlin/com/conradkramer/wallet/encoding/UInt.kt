@file:OptIn(ExperimentalUnsignedTypes::class)

package com.conradkramer.wallet.encoding

import io.ktor.utils.io.core.ByteOrder

fun UInt.toByteArray(order: ByteOrder = ByteOrder.nativeOrder(), pad: Boolean = true): ByteArray {
    return List(4) {
        val offset = when (order) {
            ByteOrder.BIG_ENDIAN -> (3 - it) * 8
            ByteOrder.LITTLE_ENDIAN -> it * 8
            else -> throw IllegalArgumentException()
        }
        (this shr offset).toUByte()
    }
        .let { bytes ->
            if (pad) {
                bytes
            } else {
                when (order) {
                    ByteOrder.BIG_ENDIAN -> bytes.dropWhile { it == 0u.toUByte() }
                    ByteOrder.LITTLE_ENDIAN -> bytes.dropLastWhile { it == 0u.toUByte() }
                    else -> throw IllegalArgumentException()
                }
            }
        }
        .toUByteArray()
        .toByteArray()
}

fun ByteArray.toUInt(order: ByteOrder = ByteOrder.nativeOrder()): UInt {
    return asUByteArray().toUInt(order)
}

fun UByteArray.toUInt(order: ByteOrder = ByteOrder.nativeOrder()): UInt {
    val size = minOf(size, 4)
    return List(size) {
        val offset = when (order) {
            ByteOrder.BIG_ENDIAN -> (size - 1 - it) * 8
            ByteOrder.LITTLE_ENDIAN -> it * 8
            else -> throw IllegalArgumentException()
        }
        (this[it].toUInt() shl offset)
    }
        .reduce(UInt::plus)
}
