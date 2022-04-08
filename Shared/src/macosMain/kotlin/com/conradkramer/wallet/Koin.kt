package com.conradkramer.wallet

import org.koin.core.Koin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

internal actual fun platformModule() = module {
    includes(darwinModule())
    factory { ViewServiceConnection(getProperty("view_service_identifier"), logger<ViewServiceConnection>()) }
    single { ViewServiceServer(getProperty("view_service_identifier"), get(), logger<ViewServiceServer>()) }
    factoryOf(::NativeMessageHost)
}

val Koin.viewServiceConnection: ViewServiceConnection
    get() = get()

val Koin.viewServiceServer: ViewServiceServer
    get() = get()

val Koin.nativeMessageHost: NativeMessageHost
    get() = get()
