@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        val androidGradleVersion = "7.1.2"
        id("com.android.application") version(androidGradleVersion)
        id("com.android.library") version(androidGradleVersion)
        kotlin("android") version("1.6.20")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://adsbynimbus-public.s3.amazonaws.com/android/sdks")
            credentials {
                username = "*"
            }
            content {
                includeGroup("com.adsbynimbus.android")
                includeGroup("com.adsbynimbus.openrtb")
                includeGroup("com.iab.omid.library.adsbynimbus")
            }
        }
    }
}

rootProject.name = "nimbus-android-sample"

include("app")
include("sample")
