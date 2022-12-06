import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    kotlin("android")
}

object Versions {
    const val accompanist = "0.24.13-rc"
    const val activity = "1.4.0"
    const val appcompat = "1.4.1"
    const val biometric = "1.2.0-alpha05"
    const val compose = "1.3.1"
    const val dbtoolsRoom = "7.0.1"
    const val koin = "3.2.2"
    const val material = "1.7.0"
    const val material3 = "1.0.1"
    const val navigation = "2.4.2"
    const val sqldelight = "2.0.0-alpha04"
}

val keystore = Properties()
    .also { it.load(FileInputStream(project.file("keystore.properties"))) }

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "com.conradkramer.wallet.android"
        minSdk = 28
        targetSdk = 33
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
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.0-alpha02"
    }

    namespace = "com.conradkramer.wallet.android"
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
    }
}

dependencies {
    implementation(project(":Shared"))
    implementation(files("../External/sqlite/sqlite-android-3400000.aar"))
    implementation("org.dbtools:dbtools-room-sqliteorg:${Versions.dbtoolsRoom}")
    implementation("app.cash.sqldelight:android-driver:${Versions.sqldelight}")
    implementation("com.google.android.material:material:${Versions.material}")
    implementation("com.google.accompanist:accompanist-systemuicontroller:${Versions.accompanist}")
    implementation("androidx.appcompat:appcompat:${Versions.appcompat}")
    implementation("androidx.biometric:biometric:${Versions.biometric}")
    implementation("androidx.biometric:biometric-ktx:${Versions.biometric}")
    implementation("androidx.compose.ui:ui:${Versions.compose}")
    implementation("androidx.compose.ui:ui-tooling:${Versions.compose}")
    implementation("androidx.compose.foundation:foundation:${Versions.compose}")
    implementation("androidx.compose.material:material-icons-core:${Versions.compose}")
    implementation("androidx.compose.material:material-icons-extended:${Versions.compose}")
    implementation("androidx.compose.material3:material3:${Versions.material3}")
    implementation("androidx.activity:activity-compose:${Versions.activity}")
    implementation("androidx.navigation:navigation-compose:${Versions.navigation}")
    implementation("io.insert-koin:koin-android:${Versions.koin}")
    implementation("io.insert-koin:koin-androidx-compose:${Versions.koin}")
}
