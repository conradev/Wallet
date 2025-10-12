import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinter)
}

val keystoreFile = project.file("keystore.properties")
val keystore = Properties().apply {
    if (keystoreFile.exists()) {
        FileInputStream(keystoreFile).use(::load)
    }
}
val hasReleaseSigning = listOf("storeFile", "storePassword", "keyAlias", "keyPassword")
    .all { keystore[it] != null }

android {
    compileSdk = 36
    defaultConfig {
        applicationId = "com.conradkramer.wallet.android"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
    }
    signingConfigs {
        create("release") {
            storeFile = keystore["storeFile"]?.let { project.file(it) }
            storePassword = keystore["storePassword"] as String?
            keyAlias = keystore["keyAlias"] as String?
            keyPassword = keystore["keyPassword"] as String?
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.toVersion(libs.versions.java.get().toInt()))
        targetCompatibility(JavaVersion.toVersion(libs.versions.java.get().toInt()))
    }

    kotlin {
        jvmToolchain(libs.versions.java.get().toInt())
    }

    buildFeatures {
        compose = true
    }

    namespace = "com.conradkramer.wallet.android"
}

dependencies {
    implementation(project(":Shared"))
    implementation(files("../External/sqlite/sqlite-android-3430000.aar"))
    implementation(libs.dbtools.room.sqliteorg)
    implementation(libs.sqldelight.driver.android)
    implementation(libs.bundles.androidx)
    implementation(libs.bundles.androidx.biometric)
    implementation(libs.bundles.koin.android)
    debugImplementation(libs.compose.ui.tooling)
}
