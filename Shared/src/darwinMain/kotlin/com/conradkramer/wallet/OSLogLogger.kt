package com.conradkramer.wallet

import kotlinx.cinterop.ptr
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import platform.darwin.OS_LOG_TYPE_DEBUG
import platform.darwin.OS_LOG_TYPE_DEFAULT
import platform.darwin.OS_LOG_TYPE_ERROR
import platform.darwin.OS_LOG_TYPE_INFO
import platform.darwin.__dso_handle
import platform.darwin._os_log_internal
import platform.darwin.os_log_create
import platform.darwin.os_log_type_t

internal class OSLogLogger(subsystem: String) : Logger() {
    private val log = os_log_create(subsystem, "Koin")

    override fun log(level: Level, msg: MESSAGE) {
        _os_log_internal(__dso_handle.ptr, log, level.logType, msg)
    }
}

internal val Level.logType: os_log_type_t
    get() = when (this) {
        Level.DEBUG -> OS_LOG_TYPE_DEBUG
        Level.INFO -> OS_LOG_TYPE_INFO
        Level.NONE -> OS_LOG_TYPE_DEFAULT
        Level.ERROR -> OS_LOG_TYPE_ERROR
    }
