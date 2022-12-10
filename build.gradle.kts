plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlinter) apply false
}

tasks.register("clean", Delete::class) {
    allprojects
        .map(Project::getBuildDir)
        .forEach { delete(it) }

    delete("External/build")
}
