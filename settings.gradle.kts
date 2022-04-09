import java.net.URI

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

sourceControl {
    gitRepository(URI("https://github.com/conradev/kotlin-logging.git")) {
        producesModule("io.github.microutils:kotlin-logging")
    }
}

rootProject.name = "Wallet"
include(":Android")
include(":Apple")
include(":Shared")
include(":External")

include(":kotlin-logging")
project(":kotlin-logging").projectDir = File("External/kotlin-logging")
