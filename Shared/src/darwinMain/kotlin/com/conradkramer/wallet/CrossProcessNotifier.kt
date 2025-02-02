package com.conradkramer.wallet

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.darwin.dispatch_get_global_queue
import platform.darwin.notify_cancel
import platform.darwin.notify_post
import platform.darwin.notify_register_dispatch
import platform.posix.QOS_CLASS_USER_INTERACTIVE

@OptIn(ExperimentalForeignApi::class)
actual class CrossProcessNotifier {
    actual fun notify(topic: String) {
        notify_post(topic)
    }

    actual fun listen(topic: String): Flow<Unit> {
        return callbackFlow {
            val token = memScoped {
                val key = alloc<IntVar>()
                notify_register_dispatch(
                    topic,
                    key.ptr,
                    dispatch_get_global_queue(QOS_CLASS_USER_INTERACTIVE.toLong(), 0u),
                ) {
                    trySend(Unit)
                }
                key.value
            }

            awaitClose { notify_cancel(token) }
        }
    }
}
