@file:Suppress("UnstableApiUsage")

import com.android.build.api.variant.BuildConfigField

plugins {
    id("nimbus.app")
}

val nimbusVersion = "2.7.0"

/* The compileSdk, minSdk, and targetSdk are applied in the build-logic/src/main/kotlin/nimbus.app.gradle.kts plugin */
android {
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    defaultConfig {
        applicationId = "com.adsbynimbus.android.sample"
        versionCode = 1
        versionName = nimbusVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
        proguardFile("r8-rules.pro")

        /* This is one example of adding keys to the application using buildConfigField */
        buildConfigField("String", "PUBLISHER_KEY", "\"${property("sample_publisher_key")}\"")
        buildConfigField("String", "API_KEY", "\"${property("sample_api_key")}\"")
    }

    namespace = "com.adsbynimbus.android.sample"
}

androidComponents.onVariants { variant ->
    variant.manifestPlaceholders.put("gamAppId",
        providers.gradleProperty("sample_gam_app_id").orElse("ca-app-pub-3940256099942544~3347511713")
    )

    /* Other keys that can be configured in the sample app */
    listOf(
            "sample_aps_app_key",
            "sample_aps_banner",
            "sample_aps_static",
            "sample_aps_video",
            "sample_fan_native_id",
            "sample_fan_interstitial_id",
            "sample_fan_banner_320_id",
            "sample_fan_native_320_id",
            "sample_gam_placement_id",
            "sample_unity_game_id",
            "sample_vungle_config_id",
            "sample_vungle_interstitial_id",
            "sample_vungle_rewarded_id",
            "sample_vungle_banner_320_id",
            "sample_vungle_mrec_id",
    ).forEach {
        variant.buildConfigFields.put(
            it.substringAfter("sample_").uppercase(),
            providers.gradleProperty(it).map { key -> BuildConfigField("String", "\"$key\"", "") },
        )
    }

    /* Fixes an issue with Coroutines 1.7.0-beta */
    variant.packaging.resources.pickFirsts.add("META-INF/versions/**")
}

dependencies {
    /* Nimbus (Version is defined by the project version above) */
    api("com.adsbynimbus.android:nimbus:$nimbusVersion")

    /* APS Demand */
    api("com.adsbynimbus.android:extension-aps:$nimbusVersion")
    api("com.amazon.android:aps-sdk:9.8.0")

    /* Meta Audience Network Demand */
    api("com.adsbynimbus.android:extension-facebook:$nimbusVersion")
    api("com.facebook.android:audience-network-sdk:6.14.0")

    /* Google Mediation Adapters and Dynamic Price */
    api("com.adsbynimbus.android:extension-google:$nimbusVersion")
    api("com.google.android.gms:play-services-ads:22.0.0")

    /* Unity Demand */
    api("com.adsbynimbus.android:extension-unity:$nimbusVersion")
    api("com.unity3d.ads:unity-ads:4.6.1")

    /* Vungle Demand */
    api("com.adsbynimbus.android:extension-vungle:$nimbusVersion")
    api("com.vungle:publisher-sdk-android:6.12.1")

    /* Androidx Libraries */
    api("androidx.activity:activity-ktx:1.7.1")
    api("androidx.annotation:annotation:1.6.0")
    api("androidx.annotation:annotation-experimental:1.3.0")
    api("androidx.appcompat:appcompat:1.6.1")
    api("androidx.core:core-ktx:1.10.0")
    api("androidx.fragment:fragment-ktx:1.5.7")
    api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    api("androidx.navigation:navigation-fragment:2.5.3")
    api("androidx.navigation:navigation-runtime:2.5.3")
    api("androidx.navigation:navigation-ui:2.5.3")
    api("androidx.preference:preference-ktx:1.2.0")
    api("androidx.recyclerview:recyclerview:1.3.0")
    api("androidx.startup:startup-runtime:1.1.1")

    /* Material */
    api("com.google.android.material:material:1.8.0")

    /* Networking Client */
    api("com.squareup.okhttp3:okhttp:4.11.0")
    api("com.squareup.okhttp3:logging-interceptor:4.11.0")

    /* Logging */
    api("com.jakewharton.timber:timber:5.0.1")

    /** Transitive Dependencies we want updated to the latest */
    api("androidx.browser:browser:1.5.0")
    api("androidx.collection:collection-ktx:1.2.0")
    api("androidx.constraintlayout:constraintlayout:2.1.4")
    api("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    api("androidx.room:room-runtime:2.5.1")
    api("androidx.transition:transition:1.4.1")
    api("androidx.work:work-runtime:2.8.1")
    api("com.squareup.okio:okio:3.3.0")

    /* Do not upgrade to 3.30.1; an error preventing the ad load needs to be resolved */
    api("com.google.ads.interactivemedia.v3:interactivemedia:3.29.0")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0-RC")
    api("org.jetbrains:annotations:24.0.1")
}
