buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", findProperty("kotlin.version").toString()))
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.3")
        classpath("org.jmailen.gradle:kotlinter-gradle:3.9.0")
        classpath("com.android.tools.build:gradle:7.1.2")
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
