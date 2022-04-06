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
        if (System.getenv("GITHUB_ACTIONS")?.toBoolean() == true) {
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
