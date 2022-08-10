@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    kotlin("android")
}

val nimbusVersion = providers.gradleProperty("nimbusVersion")

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "com.adsbynimbus.android.sample.app"
        minSdk = 21
        targetSdk = 32
        versionCode = 1
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

androidComponents.onVariants { variant ->
    variant.outputs.single {
        it.outputType == com.android.build.api.variant.VariantOutputConfiguration.OutputType.SINGLE
    }.versionName.set(nimbusVersion)
}
