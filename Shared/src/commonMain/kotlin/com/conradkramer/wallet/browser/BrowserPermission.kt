package com.conradkramer.wallet.browser

enum class BrowserPermission(val value: String) {
    ACCOUNTS("accounts");

    enum class State(val value: Int) {
        ALLOWED(1),
        DENIED(-1),
        UNSPECIFIED(0);
    }
}
