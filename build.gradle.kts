buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", findProperty("kotlin.version").toString()))
        classpath("app.cash.sqldelight:gradle-plugin:2.0.0-alpha02")
        classpath("com.android.tools.build:gradle:7.2.1")
        classpath("org.jmailen.gradle:kotlinter-gradle:3.9.0")
        classpath("com.dorongold.plugins:task-tree:2.1.0")
    }
}

apply(plugin = "com.dorongold.task-tree")

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    allprojects
        .map(Project::getBuildDir)
        .forEach { delete(it) }

    delete("External/build")
}
