package com.conradkramer.wallet.ethereum

import com.conradkramer.wallet.encoding.decodeHex
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/* ktlint-disable max-line-length */
class LogBloomFilterTests {

    @Test
    fun testBasicFunctionality() {
        val presentTopic = "0000000000000000000000008a6752a88417e8f7d822dacaeb52ed8e6e591c43".decodeHex()
        val missingTopic = "0000000000000000000000008a6752a88417e8f7d822dacaeb52ed8e6e591c44".decodeHex()
        val filter = LogBloomFilter(
            "50edef777922dbf8b5dfb55feeafef2ddc8bff46ed5d0837ae638deefef3effb98dffd5f6dec77c1d1c9faad6019054cdf7fbf7b7faffe2d67e7b77378effe59cdde60eca56bef2df9bffb1f7ffc73e0aca36956bfe63f8e7faf7f42fcefee1f374bdf74ef772af679ddd7c79a6328d3c3f9bff9fcfbfc675bea7837c09fcd9ae077753b9f7f3e9fabcd0f593fbe20d7d42daf97af2a14ff9ba9b6674dda46b8f7f900dfde83a6f2dfcf4a9fb9ae8d807e9fa229bfab7d439bfe6a2c1ecf5b37551bfe6b77ebe4f334f36bb1f7fa7d554f7c6fc35fbe7f5ce703f7a2faf363a8ddf967798ff513a7863c722cdb60da83c7fce16bedb7c9ef141bfcf1f45bbdaf".decodeHex()
        )

        assertTrue { filter.contains(presentTopic) }
        assertFalse { filter.contains(missingTopic) }
    }
}
