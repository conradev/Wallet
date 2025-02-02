import app.cash.sqldelight.core.capitalize
import com.android.build.gradle.internal.scope.ProjectInfo.Companion.getBaseName
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.native.coroutines)
    alias(libs.plugins.kotlinter)
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())

    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget(libs.versions.java.get())
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.experimental.ExperimentalObjCName")
                optIn("kotlin.experimental.ExperimentalForeignApi")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(libs.koin.core)
                api(libs.koin.annotations)
                implementation(libs.bundles.kotlinx)
                implementation(libs.bundles.ktor)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.kotlin.logging)
            }
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
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
                implementation(libs.ktor.client.darwin)
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
            }
            it.binaries {
                framework {
                    baseName = "Shared"
                }
            }
        }
    }
}

android {
    compileSdk = 35
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 28
    }
    namespace = "com.conradkramer.wallet"

    compileOptions {
        sourceCompatibility(libs.versions.java.get())
        targetCompatibility(libs.versions.java.get())
    }
}

sqldelight {
    databases {
        create("Database") {
            dialect("app.cash.sqldelight:sqlite-3-38-dialect:${libs.versions.sqldelight.get()}")
            packageName.set("com.conradkramer.wallet.sql")
        }
    }
}

dependencies {
    listOf("kspMacosX64", "kspMacosArm64", "kspIosArm64", "kspIosX64", "kspIosSimulatorArm64", "kspAndroid", "kspCommonMainMetadata")
        .forEach { add(it, libs.koin.ksp) }
}

project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
    if(name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

tasks.withType<LintTask> {
    if (name.contains("Ksp")) enabled = false
    source = source.minus(project.fileTree("build/generated")).asFileTree
}

tasks.withType<FormatTask> {
    if (name.contains("Ksp")) enabled = false
    source = source.minus(project.fileTree("build/generated")).asFileTree
}
