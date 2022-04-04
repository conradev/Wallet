pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "Wallet"
include(":Android")
include(":Apple")
include(":Shared")
include(":External")
