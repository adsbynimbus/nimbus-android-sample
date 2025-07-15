@file:Suppress("UnstableApiUsage")

import com.android.build.api.variant.BuildConfigField
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.app)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.nimbus.app)
}

android {
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.android.jvm.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.android.jvm.get())
    }

    compileSdk = libs.versions.android.sdk.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.sdk.min.get().toInt()
        targetSdk = libs.versions.android.sdk.compile.get().toInt()
        applicationId = "com.adsbynimbus.android.sample"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = 1
        versionCatalogs.named("libs").findVersion("nimbus").ifPresent {
            versionName = it.requiredVersion
        }
        proguardFile("r8-rules.pro")

        /* This is one example of adding keys to the application using buildConfigField */
        buildConfigField("String", "PUBLISHER_KEY", "\"${property("sample_publisher_key")}\"")
        buildConfigField("String", "API_KEY", "\"${property("sample_api_key")}\"")
    }

    namespace = "com.adsbynimbus.android.sample"
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(libs.versions.android.jvm.get())
    }
}

androidComponents.onVariants { variant ->
    val gamAppId = providers.gradleProperty("sample_gam_app_id")
        .orNull
        .orEmpty()
        .ifEmpty { "ca-app-pub-3940256099942544~3347511713" }
    variant.manifestPlaceholders.put("gamAppId", gamAppId)

    /* Other keys that can be configured in the sample app */
    listOf(
            "sample_admob_appid",
            "sample_admob_banner",
            "sample_admob_interstitial",
            "sample_admob_native",
            "sample_admob_rewarded",
            "sample_admob_rewarded_interstitial",
            "sample_aps_app_key",
            "sample_aps_banner",
            "sample_aps_static",
            "sample_aps_video",
            "sample_fan_native_id",
            "sample_fan_interstitial_id",
            "sample_fan_rewarded_video_id",
            "sample_fan_banner_320_id",
            "sample_fan_native_320_id",
            "sample_gam_placement_id",
            "sample_mintegral_app_id",
            "sample_mintegral_app_key",
            "sample_mintegral_banner_placement",
            "sample_mintegral_banner_adunit",
            "sample_mintegral_interstitial_placement",
            "sample_mintegral_interstitial_adunit",
            "sample_mintegral_native_placement",
            "sample_mintegral_native_adunit",
            "sample_mintegral_rewarded_placement",
            "sample_mintegral_rewarded_adunit",
            "sample_mobile_fuse_banner",
            "sample_mobile_fuse_mrec",
            "sample_mobile_fuse_interstitial",
            "sample_mobile_fuse_rewarded",
            "sample_moloco_app_key",
            "sample_moloco_banner_adunitid",
            "sample_moloco_interstitial_adunitid",
            "sample_moloco_rewarded_adunitid",
            "sample_moloco_native_adunitid",
            "sample_unity_game_id",
            "sample_vungle_config_id",
    ).forEach {
        variant.buildConfigFields?.put(
            it.substringAfter("sample_").uppercase(),
            providers.gradleProperty(it).map { key -> BuildConfigField("String", "\"$key\"", "") },
        )
    }
}

dependencies {
    /* Nimbus (Version is defined by the project version above) */
    api(libs.nimbus)

    /* Admob Demand */
    api(libs.nimbus.admob)
//    api("com.google.android.gms:play-services-ads:23.+")

    /* APS Demand */
    api(libs.nimbus.aps)
//    api("com.amazon.android:aps-sdk:9.+")

    /* Meta Audience Network Demand */
    api(libs.nimbus.meta)
//    api("com.facebook.android:audience-network-sdk:6.+")

    /* Google Mediation Adapters and Dynamic Price */
    api(libs.nimbus.google)
//    api("com.google.android.gms:play-services-ads:23.+")

    /* Dynamic Adapters for Google/AdMob */
    api(libs.nimbus.googlemediation)

    /* Mintegral Demand */
    api(libs.nimbus.mintegral)
//    api("com.mbridge.msdk.oversea:mbridge_android_sdk:16.+")

    /* Mobile Fuse Demand */
    api(libs.nimbus.mobilefuse)
//    api("com.mobilefuse.sdk:mobilefuse-sdk-core:1.+")

    api(libs.nimbus.moloco)
//    api("com.moloco.sdk:moloco-sdk:3.+")

    /* Unity Demand */
    api(libs.nimbus.unity)
//    api("com.unity3d.ads:unity-ads:4.+")

    /* Vungle Demand */
    api(libs.nimbus.vungle)
//    api("com.vungle:vungle-ads:7.+")

    /* Androidx Libraries */
    api(libs.androidx.activity)
    api(libs.androidx.annotation)
    api(libs.androidx.annotation.experimental)
    api(libs.androidx.appcompat)
    api(libs.bundles.androidx.navigation)
    api(libs.androidx.preference)
    api(libs.androidx.recyclerview)
    api(libs.androidx.startup)

    /* Networking Client */
    api(libs.okhttp)
    api(libs.okhttp.logging)

    /* Logging */
    api(libs.timber)

    /* Transitive Dependencies we want updated to the latest */
    api(libs.androidx.browser)
    api(libs.androidx.collection)
    api(libs.androidx.constraintlayout)
    api(libs.androidx.coordinatorlayout)
    api(libs.androidx.core)
    api(libs.androidx.fragment)
    api(libs.androidx.lifecycle)
    api(libs.androidx.room)
    api(libs.androidx.transition)
    api(libs.androidx.work)
    api(libs.kotlin.coroutines.android)
    api(libs.kotlin.serialization.json)
}
