package com.conradkramer.wallet

import kotlinx.coroutines.flow.Flow

expect class CrossProcessNotifier() {
    fun notify(topic: String)
    fun listen(topic: String): Flow<Unit>
}
