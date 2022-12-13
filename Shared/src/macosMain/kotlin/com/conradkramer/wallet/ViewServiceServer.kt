package com.conradkramer.wallet

import com.conradkramer.wallet.browser.BrowserMessageHost
import com.conradkramer.wallet.browser.message.Message
import com.conradkramer.wallet.browser.message.Session
import mu.KLogger
import platform.darwin.XPC_CONNECTION_MACH_SERVICE_LISTENER
import platform.darwin.XPC_TYPE_CONNECTION
import platform.darwin.XPC_TYPE_ERROR
import platform.darwin.dispatch_queue_create
import platform.darwin.xpc_connection_cancel
import platform.darwin.xpc_connection_create_mach_service
import platform.darwin.xpc_connection_resume
import platform.darwin.xpc_connection_send_message
import platform.darwin.xpc_connection_set_event_handler
import platform.darwin.xpc_connection_t
import platform.darwin.xpc_get_type
import platform.darwin.xpc_object_t

class ViewServiceServer internal constructor(
    machService: String,
    private val host: BrowserMessageHost,
    private val logger: KLogger
) {
    private val queue = dispatch_queue_create("$machService.listener", null)
    private val listener =
        xpc_connection_create_mach_service(machService, queue, XPC_CONNECTION_MACH_SERVICE_LISTENER)

    private val connections: MutableMap<Session, xpc_connection_t> = mutableMapOf()

    init {
        host.setSender { this.send(it) }
    }

    fun start() {
        xpc_connection_set_event_handler(listener) { event ->
            when (xpc_get_type(event)) {
                XPC_TYPE_ERROR -> listenerError(event)
                XPC_TYPE_CONNECTION -> incoming(event)
                else -> {}
            }
        }
        xpc_connection_resume(listener)
        logger.info { "Started view service listener" }
    }

    private fun listenerError(error: xpc_object_t) {
        logger.error { "Received listener error $error" }
    }

    private fun incoming(connection: xpc_connection_t) {
        xpc_connection_set_event_handler(connection) { event ->
            when (xpc_get_type(event)) {
                XPC_TYPE_ERROR -> connectionError(connection, event)
                else -> receive(connection, event)
            }
        }
        xpc_connection_resume(connection)
    }

    private fun connectionError(connection: xpc_connection_t, error: xpc_object_t) {
        connections
            .filterValues { it == connection }
            .keys
            .forEach { connections.remove(it) }

        logger.error { "Received error $error on $connection, ${connections.count()} connections remaining" }
    }

    private fun send(message: Message) {
        val connection = connections[message.session] ?: return
        val event = message.encodeToJsonElement().xpc
        xpc_connection_send_message(connection, event)
        logger.debug { "Sent message $event to $connection" }
    }

    private fun receive(connection: xpc_connection_t, event: xpc_object_t) {
        logger.debug { "Received message $event from $connection" }
        val message = host.decode(event.json) ?: return
        connections[message.session] = connection
        host.receive(message)
    }

    fun finalize() {
        xpc_connection_cancel(listener)
    }
}
