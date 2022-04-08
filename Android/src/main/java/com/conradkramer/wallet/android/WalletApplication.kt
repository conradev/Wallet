package com.conradkramer.wallet.android

import android.app.Application

class WalletApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin(this)
    }
}
