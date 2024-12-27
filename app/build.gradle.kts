import java.io.ByteArrayOutputStream

plugins {
    id(Plugins.androidApplication)
    kotlin(Plugins.kotlinAndroid)
    kotlin(Plugins.kapt)
    id(Plugins.kotlinParcelize)
    id(Plugins.kotlinSerialization)
    id("com.google.android.gms.oss-licenses-plugin")
    id(Plugins.googleServices) apply false
    id("com.google.firebase.crashlytics")
}

if (gradle.startParameter.taskRequests.toString().contains("Standard")) {
    apply<com.google.gms.googleservices.GoogleServicesPlugin>()
}

fun runCommand(command: String): String {
    val byteOut = ByteArrayOutputStream()
    project.exec {
        commandLine = command.split(" ")
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}

val supportedAbis = setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

android {
    compileSdk = AndroidVersions.compileSdk
    ndkVersion = AndroidVersions.ndk

    defaultConfig {
        minSdk = AndroidVersions.minSdk
        targetSdk = AndroidVersions.targetSdk
        applicationId = "eu.kanade.tachiyomi"
        versionCode = AndroidVersions.versionCode
        versionName = AndroidVersions.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true

        buildConfigField("String", "COMMIT_COUNT", "\"${getCommitCount()}\"")
        buildConfigField("String", "BETA_COUNT", "\"${getBetaCount()}\"")
        buildConfigField("String", "COMMIT_SHA", "\"${getGitSha()}\"")
        buildConfigField("String", "BUILD_TIME", "\"${getBuildTime()}\"")
        buildConfigField("Boolean", "INCLUDE_UPDATER", "false")
        buildConfigField("boolean", "BETA", "false")

        ndk {
            abiFilters += supportedAbis
        }
        externalNativeBuild {
            cmake {
                this.arguments("-DHAVE_LIBJXL=FALSE")
            }
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include(*supportedAbis.toTypedArray())
            isUniversalApk = true
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debugJ2K"
            versionNameSuffix = "-d${getCommitCount()}"
        }
        getByName("release") {
            applicationIdSuffix = ".j2k"
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
        }
        create("beta") {
            initWith(getByName("release"))
            buildConfigField("boolean", "BETA", "true")
            versionNameSuffix = "-b${getBetaCount()}"
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true

        // تعطيل بعض الأشياء غير المستخدمة
        aidl = false
        renderScript = false
        shaders = false
    }

    flavorDimensions.add("default")

    productFlavors {
        create("standard") {
            buildConfigField("Boolean", "INCLUDE_UPDATER", "true")
        }
        create("dev") {
            resourceConfigurations.clear()
            resourceConfigurations.add("en")
        }
    }

    lint {
        disable.addAll(listOf("MissingTranslation", "ExtraTranslation"))
        abortOnError = false
        checkReleaseBuilds = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    namespace = "eu.kanade.tachiyomi"
}

dependencies {
    // Compose
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.foundation:foundation:1.5.1")
    implementation("androidx.compose.animation:animation:1.5.1")
    implementation("androidx.compose.ui:ui:1.5.1")
    implementation("androidx.compose.material:material:1.5.1")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("com.google.android.material:compose-theme-adapter-3:1.1.1")
    implementation("androidx.compose.material:material-icons-extended:1.5.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.1")
    implementation("com.google.accompanist:accompanist-webview:0.30.1")
    implementation("androidx.glance:glance-appwidget:1.0.0")

    // التبعيات المعدلة
    implementation("com.github.jays2kings:subsampling-scale-image-view:756849e") {
        exclude(module = "image-decoder")
    }
    implementation("com.github.tachiyomiorg:image-decoder:7879b45")

    // مكتبات Android X
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.webkit:webkit:1.8.0")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.annotation:annotation:1.7.0")
    implementation("androidx.browser:browser:1.6.0")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.palette:palette:1.0.0")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("androidx.window:window:1.1.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.multidex:multidex:2.0.1")

    implementation(platform("com.google.firebase:firebase-bom:31.2.3"))

    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    val lifecycleVersion = "2.6.2"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-common:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    // ReactiveX
    implementation("io.reactivex:rxandroid:1.2.1")
    implementation("io.reactivex:rxjava:1.3.8")
    implementation("com.jakewharton.rxrelay:rxrelay:1.2.0")

    // Coroutines
    implementation("