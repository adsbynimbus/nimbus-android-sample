@file:Suppress("UnstableApiUsage")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        if (providers.environmentVariable("GITHUB_ACTIONS").isPresent) {
            // If running in Github Actions, use Github packages because it's free
            maven {
                url = uri("https://maven.pkg.github.com/adsbynimbus/nimbus-openrtb")
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
                includeGroupByRegex(".*\\.adsbynimbus.*")
            }
        }
    }
}

rootProject.name = "nimbus-android-sample"

include("app")
