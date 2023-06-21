package com.conradkramer.wallet

import com.conradkramer.wallet.browser.message.Message
import io.github.oshai.kotlinlogging.KLogger
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSData
import platform.darwin.XPC_ERROR_CONNECTION_INTERRUPTED
import platform.darwin.XPC_ERROR_CONNECTION_INVALID
import platform.darwin.XPC_TYPE_ERROR
import platform.darwin.xpc_connection_create_mach_service
import platform.darwin.xpc_connection_resume
import platform.darwin.xpc_connection_send_message
import platform.darwin.xpc_connection_set_event_handler
import platform.darwin.xpc_connection_t
import platform.darwin.xpc_get_type
import platform.darwin.xpc_object_t
import kotlin.native.concurrent.AtomicReference

class ViewServiceConnection(machService: String, private val logger: KLogger) {
    private class XPCConnection(val machService: String, val handler: (xpc_object_t) -> Unit) {
        private val active = AtomicReference<xpc_connection_t>(null)

        private fun connect(): xpc_connection_t {
            if (active.value != null) {
                return active.value
            }

            val connection = xpc_connection_create_mach_service(machService, null, 0)
            xpc_connection_set_event_handler(connection) { event ->
                if (xpc_get_type(event) == XPC_TYPE_ERROR &&
                    (event == XPC_ERROR_CONNECTION_INTERRUPTED || event == XPC_ERROR_CONNECTION_INVALID)
                ) {
                    active.value = null
                } else {
                    handler(event)
                }
            }
            xpc_connection_resume(connection)
            active.value = connection
            return connection
        }

        fun send(event: xpc_object_t) {
            xpc_connection_send_message(connect(), event)
        }
    }

    private var receiver: ((ByteArray) -> Unit)? = null

    private val connection = XPCConnection(machService) { event ->
        logger.debug { "Received message from view service: $event" }
        receiver?.invoke(Json.encodeToString(event.json).toByteArray())
    }

    internal fun setReceiver(receiver: (ByteArray) -> Unit) {
        this.receiver = receiver
    }

    internal fun send(data: ByteArray, browserPid: Int) {
        val event = Message.injectBrowserPid(data, browserPid, logger).xpc
        connection.send(event)
        logger.debug { "Sent message to view service: $event" }
    }

    fun setReceiver(receiver: (NSData) -> Unit) {
        this.receiver = { receiver(it.toNSData()) }
    }

    fun send(data: NSData, browserPid: Int) {
        send(data.toByteArray(), browserPid)
    }
}
