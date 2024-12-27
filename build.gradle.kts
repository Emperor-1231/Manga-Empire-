plugins {
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0" apply false
    id("com.github.ben-manes.versions") version "0.42.0" apply false
    id("org.jetbrains.kotlin.jvm") version "1.8.10" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    kotlinter {
        experimentalRules = true
        disabledRules = arrayOf("experimental:argument-list-wrapping")
    }
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.2")
        classpath("com.google.gms:google-services:4.3.15")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.8.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.4")
    }
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

tasks.named("dependencyUpdates", com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class.java).configure {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
    checkForGradleUpdate = true
    outputFormatter = "json"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}