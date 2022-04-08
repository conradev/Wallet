import org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.6.10"
    id("com.android.library")
    id("com.squareup.sqldelight")
    id("com.rickclephas.kmp.nativecoroutines") version "0.11.3-new-mm"
    id("org.jmailen.kotlinter")
}

object Versions {
    const val coroutines = "1.6.0"
    const val ktor = "2.0.0-beta-1"
    const val koin = "3.2.0-beta-1"
    const val sqldelight = "1.5.3"
    const val datetime = "0.3.2"
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
                implementation("io.ktor:ktor-client-content-negotiation:${Versions.ktor}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
                implementation("com.squareup.sqldelight:coroutines-extensions:${Versions.sqldelight}")
                implementation("io.insert-koin:koin-core:${Versions.koin}")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:${Versions.datetime}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.insert-koin:koin-test:${Versions.koin}")
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation("androidx.security:security-crypto:1.0.0")
                implementation("androidx.biometric:biometric:1.1.0")
                implementation("org.bouncycastle:bcprov-jdk15to18:1.70")
                implementation("io.ktor:ktor-client-okhttp:${Versions.ktor}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")
                implementation("androidx.biometric:biometric-ktx:1.2.0-alpha04")
                implementation("com.squareup.sqldelight:android-driver:${Versions.sqldelight}")
                implementation("io.insert-koin:koin-android:${Versions.koin}")
                implementation("io.insert-koin:koin-androidx-compose:${Versions.koin}")
            }
        }
        val androidTest by getting

        val darwinMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-ios:${Versions.ktor}")
                implementation("com.squareup.sqldelight:native-driver:${Versions.sqldelight}")
            }
        }
        val darwinDeviceMain by creating {
            dependsOn(darwinMain)
        }
        val iosSimulatorMain by creating {
            dependsOn(darwinMain)
        }

        val simulatorTargets = listOf(
            iosX64(),
            iosSimulatorArm64(),
        )
        val deviceTargets = listOf(
            iosArm64(),
            macosX64(),
            macosArm64(),
        )
        simulatorTargets.forEach {
            getByName("${it.targetName}Main") {
                dependsOn(iosSimulatorMain)
            }
        }
        deviceTargets.forEach {
            getByName("${it.targetName}Main") {
                dependsOn(darwinDeviceMain)
            }
        }
        (simulatorTargets + deviceTargets).forEach {
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
}

sqldelight {
    database("Database") {
        dialect = "sqlite:3.25"
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
