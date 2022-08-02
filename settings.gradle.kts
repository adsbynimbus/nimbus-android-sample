@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        val androidVersion: String by settings
        val kotlinVersion: String by settings
        id("com.android.application") version(androidVersion)
        id("com.android.library") version(androidVersion)
        kotlin("android") version(kotlinVersion)
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        if (providers.environmentVariable("GITHUB_ACTIONS").isPresent) {
            // If running in Github Actions, use Github packages because it's free
            maven {
                url = uri("https://maven.pkg.github.com/timehop/nimbus-openrtb")
                name = "openrtb"
                credentials(PasswordCredentials::class)
                content {
                    includeGroup("com.adsbynimbus.openrtb")
                }
            }
        }
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
