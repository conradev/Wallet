package com.conradkramer.wallet.encoding

import io.ktor.utils.io.core.ByteOrder

@OptIn(ExperimentalUnsignedTypes::class)
internal object RLP {
    internal sealed class Item {
        data class Data(val value: ByteArray) : Item() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || this::class != other::class) return false

                other as Data

                if (!value.contentEquals(other.value)) return false

                return true
            }

            override fun hashCode(): Int {
                return value.contentHashCode()
            }
        }

        data class List(val value: kotlin.collections.List<Item>) : Item() {
            constructor() : this(emptyList())
        }
    }

    fun encode(item: Item): ByteArray {
        return when (item) {
            is Item.Data -> {
                if (item.value.size == 1 && item.value.first() <= singleByte.last) {
                    item.value
                } else {
                    encodeLength(item.value.size, shortString.first) + item.value
                }
            }
            is Item.List -> {
                val items = item.value.map(RLP::encode)
                val buffer = if (items.isEmpty()) ByteArray(0) else items.reduce(ByteArray::plus)
                encodeLength(buffer.size, shortList.first) + buffer
            }
        }
    }

    private fun encodeLength(size: Int, offset: Int): ByteArray {
        return if (size < 56) {
            UByteArray(1) { (size + offset).toUByte() }.toByteArray()
        } else {
            val encodedSize = size
                .toUInt()
                .toByteArray(ByteOrder.BIG_ENDIAN, false)
            UByteArray(1) { (encodedSize.size + offset + 55).toUByte() }.toByteArray() + encodedSize
        }
    }

    fun decode(data: ByteArray): Item {
        val (item, offset) = decodeFirst(data.toUByteArray())
        if (data.size != offset) throw Exception("Invalid data")
        return item
    }

    private fun decodeFirst(data: UByteArray): Pair<Item, Int> {
        val byte = data.firstOrNull()
            ?: return (Item.List() to 0)

        return when (byte.toInt()) {
            in singleByte -> (Item.Data(UByteArray(1) { byte }.toByteArray()) to 1)
            in shortString -> {
                val size = (byte - shortString.first.toUByte()).toInt()
                (Item.Data(data.copyOfSize(1, size).toByteArray()) to 1 + size)
            }
            in longString -> {
                val sizeSize = (byte - shortString.last.toUByte()).toInt()
                val size = data.copyOfSize(1, sizeSize)
                    .toUInt(ByteOrder.BIG_ENDIAN)
                    .toInt()
                val end = 1 + sizeSize + size
                (Item.Data(data.copyOfSize(1 + sizeSize, size).toByteArray()) to end)
            }
            in shortList -> {
                val items = mutableListOf<Item>()
                val size = (byte - shortList.first.toUByte()).toInt()
                val initialOffset = 1
                var offset = initialOffset
                while (offset < size + initialOffset) {
                    val (item, itemOffset) = decodeFirst(data.copyFromOffset(offset))
                    items += item
                    offset += itemOffset
                }
                (Item.List(items) to offset)
            }
            in longList -> {
                val items = mutableListOf<Item>()
                val sizeSize = (byte - shortList.last.toUByte()).toInt()
                val size = data.copyOfSize(1, sizeSize)
                    .toUInt(ByteOrder.BIG_ENDIAN)
                    .toInt()
                val initialOffset = 1 + sizeSize
                var offset = initialOffset
                while (offset < size + initialOffset) {
                    val (item, itemOffset) = decodeFirst(data.copyFromOffset(offset))
                    items += item
                    offset += itemOffset
                }
                (Item.List(items) to offset)
            }
            else -> throw Exception()
        }
    }

    private val singleByte = 0.rangeTo(0x7F)
    private val shortString = 0x80.rangeTo(0xB7)
    private val longString = 0xB8.rangeTo(0xBF)
    private val shortList = 0xC0.rangeTo(0xF7)
    private val longList = 0xF8.rangeTo(0xFF)
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun UByteArray.copyOfSize(offset: Int, size: Int): UByteArray {
    if (size == 0) {
        return UByteArray(0)
    }

    return copyOfRange(offset, offset + size)
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun UByteArray.copyFromOffset(offset: Int): UByteArray {
    if (offset == size) {
        return UByteArray(0)
    }

    return copyOfRange(offset, size)
}
