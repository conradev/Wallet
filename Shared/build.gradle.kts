import org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.native.coroutines)
    alias(libs.plugins.kotlinter)
}

kotlin {
    android()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        val commonMain by getting {
            dependencies {
                implementation(libs.bundles.kotlinx)
                implementation(libs.bundles.ktor)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.koin)
                implementation(libs.kotlin.logging)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.koin.test)
            }
        }
        val macosTest by creating {
            dependsOn(commonTest)
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.bouncycastle)
                implementation(libs.bundles.androidx.biometric)
                implementation(libs.bundles.koin.android)
                implementation(libs.bundles.ktor.android)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.slf4j.android)
            }
        }

        val darwinMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.ktor.client.ios)
                implementation(libs.sqldelight.driver.native)
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
                    freeCompilerArgs = listOf("-linker-option", "-application_extension")
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
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 28
        targetSdk = 33
    }
    namespace = "com.conradkramer.wallet"
}

sqldelight {
    database("Database") {
        dialect("app.cash.sqldelight:sqlite-3-35-dialect:${libs.versions.sqldelight.get()}")
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
