package com.conradkramer.wallet.ethereum

private data class Cursor(val data: ByteArray) {
    private var offset = 0

    fun read(size: Int): ByteArray {
        val result = data.copyOfRange(offset, offset + size);
        offset += size
        return result
    }
}

class ABI {
    companion object {
        fun decodeString(data: Data): String {
            return decodeBytes(data).decodeToString()
        }

        fun decodeBytes(data: Data): ByteArray {
            val cursor = Cursor(data.data)
            val sizeSize = Quantity(cursor.read(32)).toInt()
            val size = Quantity(cursor.read(sizeSize)).toInt()
            return cursor.read(size)
        }

        fun supportsInterface(string: String): Data {
            return Data.fromString("0x01ffc9a7${string}00000000000000000000000000000000000000000000000000000000")
        }
    }
}
