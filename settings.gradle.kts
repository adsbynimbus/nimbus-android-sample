@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("org.chromium")
            }
        }
        mavenCentral()
        // Provides access to Nimbus SDK artifacts
        maven("https://adsbynimbus-public.s3.amazonaws.com/android/sdks") {
            content {
                includeGroupByRegex(".*\\.adsbynimbus.*")
            }
        }
        // Provides access to the LiveRamp SDK
        maven("https://sdk-android-prod.launch.liveramp.com") {
            content {
                includeGroupByRegex(".*\\.liveramp.*")
            }
        }
        // Provides access to the Mintegral SDK
        maven("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea") {
            content {
                includeGroupByRegex(".*\\.mbridge.*")
            }
        }
    }
}

rootProject.name = "nimbus-android-sample"

include("app")
