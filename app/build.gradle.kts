@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "com.adsbynimbus.android.sample.app"
        minSdk = 21
        targetSdk = 32
        versionCode = 1
        versionName = "${project(":sample").version}"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":sample"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
}
