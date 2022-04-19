package com.conradkramer.wallet.bigint

import gmp.__mpf_struct
import gmp.mpf_clear
import gmp.mpf_div
import gmp.mpf_get_d
import gmp.mpf_init
import gmp.mpf_set_d
import gmp.mpf_set_z
import kotlinx.cinterop.alloc
import kotlinx.cinterop.invoke
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr

actual class BigDecimal() {
    val mpf: __mpf_struct = nativeHeap.alloc()

    init {
        mpf_init!!(mpf.ptr)
    }

    fun finalize() {
        mpf_clear!!(mpf.ptr)
        nativeHeap.free(mpf.rawPtr)
    }

    actual constructor(integer: BigInteger) : this() {
        mpf_set_z!!(mpf.ptr, integer.mpz.ptr)
    }

    actual fun toDouble(): Double {
        return mpf_get_d!!(mpf.ptr)
    }

    actual operator fun div(other: BigDecimal): BigDecimal {
        val result = BigDecimal()
        mpf_div!!(result.mpf.ptr, mpf.ptr, other.mpf.ptr)
        return result
    }

    actual companion object {
        actual fun valueOf(value: Double): BigDecimal {
            val result = BigDecimal()
            mpf_set_d!!(result.mpf.ptr, value)
            return result
        }
    }
}
