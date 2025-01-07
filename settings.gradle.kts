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
        if (providers.environmentVariable("GITHUB_ACTIONS").isPresent) {
            // If running in Github Actions, use Github packages because it's free
            maven("https://maven.pkg.github.com/timehop/nimbus-openrtb") {
                name = "openrtb"
                credentials(PasswordCredentials::class)
                content {
                    includeGroup("com.adsbynimbus.openrtb")
                }
            }
        }
        // Provides access to Nimbus SDK artifacts
        maven("https://adsbynimbus-public.s3.amazonaws.com/android/sdks") {
            content {
                includeGroupByRegex(".*\\.adsbynimbus.*")
            }
        }
    }
}

rootProject.name = "nimbus-android-sample"

include("app")
