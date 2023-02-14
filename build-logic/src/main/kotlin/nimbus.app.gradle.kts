plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }

    /* Ensures the jvmTarget is aligned at 11 for both languages */
    compileOptions.targetCompatibility = JavaVersion.VERSION_11.also {
        kotlinOptions.jvmTarget = it.toString()
    }
}
