package com.conradkramer.wallet.bigint

import com.conradkramer.wallet.encoding.RLP
import com.conradkramer.wallet.encoding.RLPRepresentable
import gmp.__mpz_struct
import gmp._mpz_cmp_si
import gmp.mpf_set_z
import gmp.mpz_add
import gmp.mpz_clear
import gmp.mpz_cmp
import gmp.mpz_export
import gmp.mpz_get_si
import gmp.mpz_get_ui
import gmp.mpz_import
import gmp.mpz_init
import gmp.mpz_set_si
import gmp.mpz_set_ui
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
actual class BigInteger() : RLPRepresentable {
    val mpz: __mpz_struct = nativeHeap.alloc()

    init { mpz_init!!(mpz.ptr) }

    fun finalize() {
        mpz_clear!!(mpz.ptr)
        nativeHeap.free(mpz.rawPtr)
    }

    actual val data: ByteArray
        get() = memScoped {
            if (_mpz_cmp_si!!(mpz.ptr, 0) == 0) return byteArrayOf()

            val count = alloc<size_tVar>()
            val size = (mpz_sizeinbase!!(mpz.ptr, 2) + 7u) / 8u
            val data = ByteArray(size.convert())
            data.asUByteArray().usePinned { pinned ->
                mpz_export!!(
                    pinned.addressOf(0),
                    count.ptr,
                    1.convert(),
                    data.size.convert(),
                    1.convert(),
                    0.convert(),
                    mpz.ptr
                )
            }
            if (count.value != 1UL) throw Exception("Failed to export integer")
            data
        }

    override val rlp: RLP.Item
        get() = RLP.Item.Data(data)

    actual constructor(data: ByteArray) : this() {
        if (data.isEmpty()) return
        data.asUByteArray().usePinned { pinned ->
            mpz_import!!(
                mpz.ptr,
                1.convert(),
                1.convert(),
                data.size.convert(),
                1.convert(),
                0.convert(),
                pinned.addressOf(0)
            )
        }
    }

    actual fun toBigDecimal() = BigDecimal().also { mpf_set_z!!(it.mpf.ptr, mpz.ptr) }
    actual fun toLong() = mpz_get_si!!(mpz.ptr)
    actual fun toULong() = mpz_get_ui!!(mpz.ptr)

    actual operator fun plus(valueOf: BigInteger): BigInteger = BigInteger().also { mpz_add!!(it.mpz.ptr, this.mpz.ptr, valueOf.mpz.ptr) }

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BigInteger

        if (mpz_cmp!!(mpz.ptr, other.mpz.ptr) != 0) return false

        return true
    }

    actual override fun hashCode() = mpz.hashCode()

    actual companion object {
        actual fun valueOf(value: Long): BigInteger = BigInteger().also { mpz_set_si!!(it.mpz.ptr, value) }
        actual fun valueOf(value: ULong): BigInteger = BigInteger().also { mpz_set_ui!!(it.mpz.ptr, value) }
    }
}
