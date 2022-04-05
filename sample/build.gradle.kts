plugins {
    id("com.android.library")
    kotlin("android")
    id("androidx.navigation.safeargs") version("2.4.1")
}

android {
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "PUBLISHER_KEY", "\"${property("sample_publisher_key")}\"")
        buildConfigField("String", "API_KEY", "\"${property("sample_api_key")}\"")
        buildConfigField("String", "APS_APP_KEY", "\"${property("sample_aps_app_key")}\"")
        buildConfigField("String", "APS_BANNER", "\"${property("sample_aps_banner")}\"")
        buildConfigField("String", "APS_STATIC", "\"${property("sample_aps_static")}\"")
        buildConfigField("String", "APS_VIDEO", "\"${property("sample_aps_video")}\"")
        buildConfigField("String", "FAN_NATIVE_ID", "\"${property("sample_fan_native")}\"")
        buildConfigField("String", "FAN_INTERSTITIAL_ID", "\"${property("sample_fan_interstitial")}\"")
        buildConfigField("String", "FAN_BANNER_320_ID", "\"${property("sample_fan_banner_320")}\"")
        buildConfigField("String", "FAN_NATIVE_320_ID", "\"${property("sample_fan_native_320")}\"")
        buildConfigField("String", "MOPUB_BANNER_ID", "\"${property("sample_mopub_banner_id")}\"")
        buildConfigField("String", "MOPUB_MREC_ID", "\"${property("sample_mopub_mrec_id")}\"")
        buildConfigField("String", "MOPUB_INTERSTITIAL_ID", "\"${property("sample_mopub_interstitial_id")}\"")
        buildConfigField("String", "GAM_PLACEMENT_ID", "\"${property("sample_gam_placement_id")}\"")
        buildConfigField("String", "UNITY_GAME_ID", "\"${property("sample_unity_game_id")}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Startup
    api("androidx.startup:startup-runtime:1.1.1")

    // Nimbus
    val nimbusVersion = "1.11.3"
    implementation("com.adsbynimbus.android:nimbus:$nimbusVersion")
    implementation("com.adsbynimbus.android:extension-aps:$nimbusVersion")
    implementation("com.adsbynimbus.android:extension-exoplayer:$nimbusVersion")
    implementation("com.adsbynimbus.android:extension-facebook:$nimbusVersion")
    implementation("com.adsbynimbus.android:extension-google:$nimbusVersion")
    implementation("com.adsbynimbus.android:extension-mopub:$nimbusVersion")
    implementation("com.adsbynimbus.android:extension-okhttp:$nimbusVersion")
    implementation("com.adsbynimbus.android:extension-viewability:$nimbusVersion")
    implementation("com.adsbynimbus.android:extension-unity:$nimbusVersion")

    // Aps
    implementation("com.amazon.android:aps-sdk:9.4.2")

    // Facebook
    implementation("com.facebook.android:audience-network-sdk:6.8.0")

    // Google
    implementation("com.google.android.gms:play-services-ads:20.6.0")

    // MoPub
    implementation("com.mopub:mopub-sdk-banner:5.18.0")
    implementation("com.mopub:mopub-sdk-fullscreen:5.18.0")

    // Core, Fragment, AppCompat
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.fragment:fragment-ktx:1.4.1")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.4.1")
    implementation("androidx.navigation:navigation-runtime-ktx:2.4.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.4.1")

    // Preferences
    implementation("androidx.preference:preference-ktx:1.2.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // Material
    implementation("com.google.android.material:material:1.5.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    constraints {
        implementation("androidx.activity:activity:1.4.0")
        implementation("androidx.activity:activity-ktx:1.4.0")
        implementation("androidx.annotation:annotation-experimental:1.2.0")
        implementation("androidx.browser:browser:1.4.0")
        implementation("androidx.collection:collection:1.2.0")
        implementation("androidx.collection:collection-ktx:1.2.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.3")
        implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
        implementation("androidx.lifecycle:lifecycle-livedata:2.4.1")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
        implementation("androidx.media2:media2-player:1.2.1")
        implementation("androidx.media2:media2-widget:1.2.1")
        implementation("androidx.media:media:1.5.0")
        implementation("androidx.room:room-runtime:2.4.2")
        implementation("androidx.transition:transition:1.4.1")
        implementation("androidx.work:work-runtime:2.7.1")
        implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")
        implementation("com.google.ads.interactivemedia.v3:interactivemedia:3.27.0")
        implementation("com.squareup.okio:okio:3.0.0")
        implementation("org.jetbrains:annotations:23.0.0")
    }
}
