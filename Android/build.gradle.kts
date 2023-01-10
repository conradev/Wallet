import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinter)
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
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
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
    implementation(libs.dbtools.room.sqliteorg)
    implementation(libs.sqldelight.driver.android)
    implementation(libs.bundles.accompanist)
    implementation(libs.bundles.androidx)
    implementation(libs.bundles.androidx.biometric)
    implementation(libs.bundles.koin.android)
    debugImplementation(libs.compose.ui.tooling)
}
