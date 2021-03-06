plugins {
    id("com.android.application")
    kotlin("android")
}

object Versions {
    const val activity = "1.4.0"
    const val appcompat = "1.4.1"
    const val biometric = "1.2.0-alpha04"
    const val compose = "1.2.0-rc03"
    const val dbtoolsRoom = "7.0.1"
    const val koin = "3.2.0-beta-1"
    const val material = "1.6.1"
    const val material3 = "1.0.0-alpha13"
    const val navigation = "2.4.2"
    const val sqldelight = "2.0.0-alpha03"
}

android {
    compileSdk = 32
    defaultConfig {
        applicationId = "com.conradkramer.wallet.android"
        minSdk = 28
        targetSdk = 32
        versionCode = 1
        versionName = "0.1"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
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
        kotlinCompilerExtensionVersion = "1.2.0"
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
    implementation(files("../External/sqlite/sqlite-android-3380300.aar"))
    implementation("org.dbtools:dbtools-room-sqliteorg:${Versions.dbtoolsRoom}")
    implementation("app.cash.sqldelight:android-driver:${Versions.sqldelight}")
    implementation("com.google.android.material:material:${Versions.material}")
    implementation("androidx.appcompat:appcompat:${Versions.appcompat}")
    implementation("androidx.biometric:biometric:${Versions.biometric}")
    implementation("androidx.biometric:biometric-ktx:${Versions.biometric}")
    implementation("androidx.compose.ui:ui:${Versions.compose}")
    implementation("androidx.compose.ui:ui-tooling:${Versions.compose}")
    implementation("androidx.compose.foundation:foundation:${Versions.compose}")
    implementation("androidx.compose.material:material:${Versions.compose}")
    implementation("androidx.compose.material:material-icons-core:${Versions.compose}")
    implementation("androidx.compose.material:material-icons-extended:${Versions.compose}")
    implementation("androidx.compose.material3:material3:${Versions.material3}")
    implementation("androidx.activity:activity-compose:${Versions.activity}")
    implementation("androidx.navigation:navigation-compose:${Versions.navigation}")
    implementation("io.insert-koin:koin-android:${Versions.koin}")
    implementation("io.insert-koin:koin-androidx-compose:${Versions.koin}")
}
