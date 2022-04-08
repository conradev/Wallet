package com.conradkramer.wallet.bigint

import gmp.__mpz_struct
import gmp.mpz_clear
import gmp.mpz_export
import gmp.mpz_import
import gmp.mpz_init
import gmp.mpz_sizeinbase
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.posix.size_tVar

@OptIn(ExperimentalUnsignedTypes::class)
actual class BigInteger() {
    val mpz: __mpz_struct = nativeHeap.alloc()

    init {
        mpz_init!!(mpz.ptr)
    }

    actual val data: ByteArray
        get() = memScoped {
            val count = alloc<size_tVar>()
            val size = (mpz_sizeinbase!!(mpz.ptr, 2) + 7u) / 8u
            val data = ByteArray(size.convert())
            data.asUByteArray()
                .usePinned { pinnedData ->
                    mpz_export!!(
                        pinnedData.addressOf(0),
                        count.ptr,
                        1.convert(),
                        data.size.convert(),
                        1.convert(),
                        0.convert(),
                        mpz.ptr
                    )
                }
            if (count.value.toInt() != 1) throw Exception("Failed to export integer")
            data
        }

    fun finalize() {
        mpz_clear!!(mpz.ptr)
        nativeHeap.free(mpz.rawPtr)
    }

    actual constructor(data: ByteArray) : this() {
        data.asUByteArray().usePinned { pinnedData ->
            mpz_import!!(
                mpz.ptr,
                1.convert(),
                1.convert(),
                data.size.convert(),
                1.convert(),
                0.convert(),
                pinnedData.addressOf(0)
            )
        }
    }
}
