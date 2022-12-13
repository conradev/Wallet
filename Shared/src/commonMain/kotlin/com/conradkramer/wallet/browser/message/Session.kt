package com.conradkramer.wallet.browser.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Session(
    @SerialName("browser_pid")
    val browserPid: Int,
    @SerialName("tab_id")
    val tabId: Int,
    @SerialName("frame_id")
    val frameId: Int
) {
    internal fun browserPid(browserPid: Int): Session {
        return Session(browserPid, this.tabId, this.frameId)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Session

        if (browserPid != other.browserPid) return false
        if (tabId != other.tabId) return false
        if (frameId != other.frameId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = browserPid
        result = 31 * result + tabId
        result = 31 * result + frameId
        return result
    }
}
