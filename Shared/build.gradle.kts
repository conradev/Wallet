import org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.6.21"
    id("com.android.library")
    id("app.cash.sqldelight")
    id("com.rickclephas.kmp.nativecoroutines") version "0.12.2-new-mm"
    id("org.jmailen.kotlinter")
}

object Versions {
    const val biometric = "1.2.0-alpha04"
    const val bouncycastle = "1.71"
    const val coroutines = "1.6.1"
    const val ktor = "2.0.0"
    const val koin = "3.2.0-beta-1"
    const val serialization = "1.3.2"
    const val slf4j = "1.7.36"
    const val sqldelight = "2.0.0-alpha02"
}

kotlin {
    android()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}") {
                    version {
                        strictly(Versions.coroutines)
                    }
                }
                implementation("io.ktor:ktor-client-core:${Versions.ktor}")
                implementation("io.ktor:ktor-client-logging:${Versions.ktor}")
                implementation("io.ktor:ktor-client-content-negotiation:${Versions.ktor}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serialization}")
                implementation("app.cash.sqldelight:coroutines-extensions:${Versions.sqldelight}")
                implementation("io.insert-koin:koin-core:${Versions.koin}")
                implementation(project(":kotlin-logging"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.insert-koin:koin-test:${Versions.koin}")
            }
        }
        val macosTest by creating {
            dependsOn(commonTest)
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("androidx.biometric:biometric:${Versions.biometric}")
                implementation("androidx.biometric:biometric-ktx:${Versions.biometric}")
                implementation("org.bouncycastle:bcprov-jdk15to18:${Versions.bouncycastle}")
                implementation("io.ktor:ktor-client-okhttp:${Versions.ktor}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")
                implementation("io.insert-koin:koin-android:${Versions.koin}")
                implementation("io.insert-koin:koin-androidx-compose:${Versions.koin}")
                implementation("org.slf4j:slf4j-android:${Versions.slf4j}")
            }
        }
        val androidTest by getting

        val darwinMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-ios:${Versions.ktor}")
                implementation("app.cash.sqldelight:native-driver:${Versions.sqldelight}")
            }
        }
        val macosMain by creating {
            dependsOn(darwinMain)
        }
        val iosMain by creating {
            dependsOn(darwinMain)
        }
        val iosDeviceMain by creating {
            dependsOn(iosMain)
        }
        val iosSimulatorMain by creating {
            dependsOn(iosMain)
        }

        val macosTargets = listOf(
            macosX64(),
            macosArm64(),
        )
        val iosDeviceTargets = listOf(
            iosArm64()
        )
        val iosSimulatorTargets = listOf(
            iosX64(),
            iosSimulatorArm64(),
        )
        val nativeTargets = macosTargets + iosDeviceTargets + iosSimulatorTargets
        macosTargets.forEach {
            getByName("${it.targetName}Main") {
                dependsOn(macosMain)
            }
            getByName("${it.targetName}Test") {
                dependsOn(macosTest)
            }
        }
        iosDeviceTargets.forEach {
            getByName("${it.targetName}Main") {
                dependsOn(iosDeviceMain)
            }
        }
        iosSimulatorTargets.forEach {
            getByName("${it.targetName}Main") {
                dependsOn(iosSimulatorMain)
            }
        }
        nativeTargets.forEach {
            it.compilations.getByName("main") {
                cinterops {
                    val gmp by creating
                    val ripemd160 by creating
                    val secp256k1 by creating
                    val xkcp by creating
                }
                kotlinOptions {
                    freeCompilerArgs = listOf("-linker-options", "-application_extension")
                }
                enableEndorsedLibs = true
            }
            it.binaries {
                framework {
                    baseName = "Shared"
                    embedBitcode(BitcodeEmbeddingMode.DISABLE)
                }
            }
        }
    }
}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 28
        targetSdk = 31
    }
    namespace = "com.conradkramer.wallet"
}

sqldelight {
    database("Database") {
        dialect = "app.cash.sqldelight:sqlite-3-35-dialect:${Versions.sqldelight}"
        packageName = "com.conradkramer.wallet.sql"
    }
}

tasks.lintKotlinCommonMain {
    exclude("com/conradkramer/wallet/data/**/*.kt")
    exclude("com/conradkramer/wallet/sql/**/*.kt")
}
tasks.formatKotlinCommonMain {
    exclude("com/conradkramer/wallet/data/**/*.kt")
    exclude("com/conradkramer/wallet/sql/**/*.kt")
}
