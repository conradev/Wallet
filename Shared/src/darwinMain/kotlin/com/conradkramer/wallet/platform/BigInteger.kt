package com.conradkramer.wallet.platform

import gmp.__mpz_struct
import gmp.mpz_clear
import gmp.mpz_import
import gmp.mpz_init
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.invoke
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned

@OptIn(ExperimentalUnsignedTypes::class)
actual class BigInteger() {
    val mpz: __mpz_struct = nativeHeap.alloc()

    init {
        mpz_init!!(mpz.ptr)
    }

    fun finalize() {
        mpz_clear!!(mpz.ptr)
        nativeHeap.free(mpz.rawPtr)
    }

    actual constructor(bytes: ByteArray) : this() {
        bytes.asUByteArray().usePinned { pinnedBytes ->
            mpz_import!!(
                mpz.ptr,
                1.convert(),
                1.convert(),
                bytes.size.convert(),
                1.convert(),
                0.convert(),
                pinnedBytes.addressOf(0)
            )
        }
    }
}
