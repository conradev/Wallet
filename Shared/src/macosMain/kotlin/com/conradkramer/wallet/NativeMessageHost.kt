package com.conradkramer.wallet

import com.conradkramer.wallet.encoding.toByteArray
import com.conradkramer.wallet.encoding.toUInt
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.posix.EINTR
import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO
import platform.posix.errno
import platform.posix.getppid
import platform.posix.read
import platform.posix.write

@OptIn(ExperimentalUnsignedTypes::class)
class NativeMessageHost(private val connection: ViewServiceConnection) {

    val parentPid: Int = getppid()

    init {
        connection.setReceiver { data: ByteArray ->
            write(STDOUT_FILENO, data.size.toUInt().toByteArray() + data)
        }
    }

    fun run() {
        while (true) {
            val length = read(STDIN_FILENO, 4).toUInt().toInt()
            val data = read(STDIN_FILENO, length)
            connection.send(data, parentPid)
        }
    }

    private fun write(fd: Int, data: ByteArray) {
        data.asUByteArray().usePinned { pinnedData ->
            if (write(fd, pinnedData.addressOf(0), data.size.convert()) >= 0.toLong()) {
                return
            }

            when (val error = errno) {
                EINTR -> {}
                else -> throw Exception("Read failed with errno $error")
            }
        }
    }

    private fun read(fd: Int, size: Int): ByteArray {
        var index = 0
        val data = ByteArray(size)
        while (index < size) {
            data.asUByteArray().usePinned { pinnedData ->
                val read = read(
                    fd,
                    pinnedData.addressOf(index),
                    (size - index).convert()
                ).toInt()
                if (read >= 0) {
                    index += read
                    return@usePinned
                }

                when (val error = errno) {
                    EINTR -> {}
                    else -> throw Exception("Read failed with errno $error")
                }
            }
        }
        return data
    }
}
