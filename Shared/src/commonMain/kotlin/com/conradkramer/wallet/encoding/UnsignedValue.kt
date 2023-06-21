@file:OptIn(ExperimentalUnsignedTypes::class)

package com.conradkramer.wallet.encoding

import io.ktor.utils.io.core.ByteOrder

fun UInt.toByteArray(order: ByteOrder = ByteOrder.nativeOrder(), pad: Boolean = true) = encodeByteArray(
    this,
    order,
    pad,
    UInt.SIZE_BYTES,
    UInt::shr,
    UInt::toUByte,
)
fun ULong.toByteArray(order: ByteOrder = ByteOrder.nativeOrder(), pad: Boolean = true) = encodeByteArray(
    this,
    order,
    pad,
    ULong.SIZE_BYTES,
    ULong::shr,
    ULong::toUByte,
)

private inline fun <reified T> encodeByteArray(
    input: T,
    order: ByteOrder,
    pad: Boolean,
    size: Int,
    shr: (T, Int) -> T,
    cast: (T) -> UByte,
): ByteArray {
    return List(size) {
        val offset = when (order) {
            ByteOrder.BIG_ENDIAN -> (size - 1 - it) * 8
            ByteOrder.LITTLE_ENDIAN -> it * 8
            else -> throw IllegalArgumentException()
        }
        cast(shr(input, offset))
    }
        .let { bytes ->
            if (pad) {
                bytes
            } else {
                when (order) {
                    ByteOrder.BIG_ENDIAN -> bytes.dropWhile { it == 0.toUByte() }
                    ByteOrder.LITTLE_ENDIAN -> bytes.dropLastWhile { it == 0.toUByte() }
                    else -> throw IllegalArgumentException()
                }
            }
        }
        .toUByteArray()
        .toByteArray()
}

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.toUInt(order: ByteOrder = ByteOrder.nativeOrder()) = asUByteArray().toUInt(order)

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.toULong(order: ByteOrder = ByteOrder.nativeOrder()) = asUByteArray().toULong(order)

fun UByteArray.toUInt(order: ByteOrder = ByteOrder.nativeOrder()) = toUnsignedValue(
    order,
    UInt.SIZE_BYTES,
    UByte::toUInt,
    UInt::shl,
    UInt::plus,
    0U,
)
fun UByteArray.toULong(order: ByteOrder = ByteOrder.nativeOrder()) = toUnsignedValue(
    order,
    ULong.SIZE_BYTES,
    UByte::toULong,
    ULong::shl,
    ULong::plus,
    0UL,
)

private inline fun <reified T> UByteArray.toUnsignedValue(
    order: ByteOrder,
    typeSize: Int,
    cast: (UByte) -> T,
    shl: (T, Int) -> T,
    plus: (T, T) -> T,
    zero: T,
): T {
    if (isEmpty()) return zero
    val size = minOf(size, typeSize)
    return List(size) {
        val offset = when (order) {
            ByteOrder.BIG_ENDIAN -> (size - 1 - it) * 8
            ByteOrder.LITTLE_ENDIAN -> it * 8
            else -> throw IllegalArgumentException()
        }
        shl(cast(this[it]), offset)
    }
        .reduce(plus)
}
