package com.conradkramer.wallet.bigint

import gmp.__mpf_struct
import gmp.mpf_clear
import gmp.mpf_div
import gmp.mpf_floor
import gmp.mpf_get_d
import gmp.mpf_init
import gmp.mpf_mul
import gmp.mpf_set_d
import gmp.mpz_set_f
import kotlinx.cinterop.alloc
import kotlinx.cinterop.invoke
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr

actual class BigDecimal {
    val mpf: __mpf_struct = nativeHeap.alloc()

    init {
        mpf_init!!(mpf.ptr)
    }

    fun finalize() {
        mpf_clear!!(mpf.ptr)
        nativeHeap.free(mpf.rawPtr)
    }

    actual fun toDouble() = mpf_get_d!!(mpf.ptr)
    actual fun toBigInteger() = BigInteger().also { mpz_set_f!!(it.mpz.ptr, mpf.ptr) }

    private fun floor() = BigDecimal().also { mpf_floor!!(it.mpf.ptr, mpf.ptr) }

    private fun divide(other: BigDecimal) = BigDecimal().also { mpf_div!!(it.mpf.ptr, mpf.ptr, other.mpf.ptr) }

    actual fun div(other: BigDecimal, scale: Int) = (this.divide(other) * pow10(scale)).floor().divide(pow10(scale))
    actual operator fun div(other: BigDecimal) = div(other, CURRENCY_SCALE)

    actual operator fun times(valueOf: BigDecimal) = BigDecimal()
        .also { mpf_mul!!(it.mpf.ptr, mpf.ptr, valueOf.mpf.ptr) }

    actual companion object {
        actual fun valueOf(value: Double) = BigDecimal().also { mpf_set_d!!(it.mpf.ptr, value) }
    }
}
