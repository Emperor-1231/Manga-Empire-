import java.util.Locale

// معلومات عن نسخ SDK و Kotlin
object AndroidVersions {
    const val compileSdk = 34
    const val minSdk = 23
    const val targetSdk = 34
    const val versionCode = 111
    const val versionName = "1.7.4"
    const val ndk = "23.1.7779620"
    const val kotlin = "1.9.10"
}

// قائمة الإضافات المستخدمة في المشروع
object Plugins {
    const val androidApplication = "com.android.application"
    const val googleServices = "com.google.gms.google-services"
    const val kapt = "kapt"
    const val kotlinParcelize = "kotlin-parcelize"
    const val kotlinAndroid = "android"
    const val jetbrainsKotlin = "org.jetbrains.kotlin.android"
    const val kotlinSerialization = "org.jetbrains.kotlin.plugin.serialization"
    val gradleVersions = PluginClass("com.github.ben-manes.versions", "0.42.0")
    val kotlinter = PluginClass("org.jmailen.kotlinter", "3.12.0")
}

// تعريف فئة لإدارة الإضافات
data class PluginClass(val name: String, val version: String) {
    override fun toString() = "$name:$version"
}

// التحقق مما إذا كان الإصدار مستقرًا أم لا
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase(Locale.ROOT).contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}