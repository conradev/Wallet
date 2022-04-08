package com.conradkramer.wallet

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual class CrossProcessNotifier {
    actual fun notify(topic: String) {
    }

    actual fun listen(topic: String): Flow<Unit> {
        return flowOf(Unit)
    }
}
