package com.conradkramer.wallet.android

import android.app.Application
import com.conradkramer.wallet.startKoin

class WalletApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin(this)
    }
}
