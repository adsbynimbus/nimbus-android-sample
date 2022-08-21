@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.application") version("7.2.2")
        kotlin("android") version("1.7.10")
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
