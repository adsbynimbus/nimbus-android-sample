plugins {
    id("com.android.application")
    kotlin("android")
    id("androidx.navigation.safeargs")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "com.adsbynimbus.android.sample.app"
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.11.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":sample"))
}
