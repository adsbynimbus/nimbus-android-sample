@file:Suppress("UnstableApiUsage")

import com.android.build.api.variant.BuildConfigField

plugins {
    id("nimbus.app")
}

val nimbusVersion = "2.0.0"

/* The compileSdk, minSdk, and targetSdk are applied in the build-logic/src/main/kotlin/nimbus.app.gradle.kts plugin */
android {
    defaultConfig {
        applicationId = "com.adsbynimbus.android.sample"
        versionCode = 1
        versionName = nimbusVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true

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
    ).forEach {
        variant.buildConfigFields.put(
                it.substringAfter("sample_").toUpperCase(),
                providers.gradleProperty(it).map { key -> BuildConfigField("String", "\"$key\"", "") },
        )
    }
}

dependencies {
    /* Nimbus (Version is defined by the project version above) */
    api("com.adsbynimbus.android:nimbus:$nimbusVersion")
    api("com.adsbynimbus.android:extension-aps:$nimbusVersion")
    api("com.adsbynimbus.android:extension-facebook:$nimbusVersion")
    api("com.adsbynimbus.android:extension-google:$nimbusVersion")
    api("com.adsbynimbus.android:extension-viewability:$nimbusVersion")
    api("com.adsbynimbus.android:extension-unity:$nimbusVersion")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    /* Androidx Libraries */
    api("androidx.activity:activity-ktx:1.6.0")
    api("androidx.annotation:annotation:1.5.0")
    api("androidx.annotation:annotation-experimental:1.3.0")
    api("androidx.appcompat:appcompat:1.5.1")
    api("androidx.core:core-ktx:1.9.0")
    api("androidx.fragment:fragment-ktx:1.5.3")
    api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    api("androidx.navigation:navigation-fragment-ktx:2.5.2")
    api("androidx.navigation:navigation-runtime-ktx:2.5.2")
    api("androidx.navigation:navigation-ui-ktx:2.5.2")
    api("androidx.preference:preference-ktx:1.2.0")
    api("androidx.recyclerview:recyclerview:1.2.1")
    api("androidx.startup:startup-runtime:1.1.1")

    /* Material */
    api("com.google.android.material:material:1.6.1")

    /* Networking Client */
    api("com.squareup.okhttp3:okhttp:4.10.0")
    api("com.squareup.okhttp3:logging-interceptor:4.10.0")

    /* Logging */
    api("com.jakewharton.timber:timber:5.0.1")

    constraints {
        api("androidx.activity:activity:1.6.0")
        api("androidx.browser:browser:1.4.0")
        api("androidx.collection:collection:1.2.0")
        api("androidx.collection:collection-ktx:1.2.0")
        api("androidx.constraintlayout:constraintlayout:2.1.4")
        api("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
        api("androidx.lifecycle:lifecycle-livedata:2.5.1")
        api("androidx.lifecycle:lifecycle-runtime:2.5.1")
        api("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
        api("androidx.media2:media2-player:1.2.1")
        api("androidx.media2:media2-widget:1.2.1")
        api("androidx.media:media:1.6.0")
        api("androidx.room:room-runtime:2.4.3")
        api("androidx.transition:transition:1.4.1")
        api("androidx.work:work-runtime:2.7.1")
        api("com.google.ads.interactivemedia.v3:interactivemedia:3.28.2")
        api("com.google.android.gms:play-services-ads:21.2.0")
        api("com.squareup.okio:okio:3.2.0")
        api("org.jetbrains:annotations:23.0.0")
    }
}
