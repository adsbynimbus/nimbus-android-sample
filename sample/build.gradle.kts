import com.android.build.api.dsl.LibraryDefaultConfig
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toUpperCaseAsciiOnly

plugins {
    id("com.android.library")
    kotlin("android")
    id("androidx.navigation.safeargs") version("2.4.1")
}

// Demonstrates the usage of extension functions and gradle providers to add a buildConfigField
fun LibraryDefaultConfig.addSampleBuildConfigField(name: String) {
    buildConfigField("String", name.substringAfter("_").toUpperCaseAsciiOnly(),
        "\"${providers.gradleProperty(name).getOrElse("")}\"")
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

        // This is one example of adding keys to the application using buildConfigField
        buildConfigField("String", "PUBLISHER_KEY", "\"${property("sample_publisher_key")}\"")
        buildConfigField("String", "API_KEY", "\"${property("sample_api_key")}\"")

        // The following uses buildConfigField and gradle property providers to add keys
        addSampleBuildConfigField("sample_aps_app_key")
        addSampleBuildConfigField("sample_aps_banner")
        addSampleBuildConfigField("sample_aps_static")
        addSampleBuildConfigField("sample_aps_video")
        addSampleBuildConfigField("sample_fan_native_id")
        addSampleBuildConfigField("sample_fan_interstitial_id")
        addSampleBuildConfigField("sample_fan_banner_320_id")
        addSampleBuildConfigField("sample_fan_native_320_id")
        addSampleBuildConfigField("sample_gam_placement_id")
        addSampleBuildConfigField("sample_unity_game_id")
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
    implementation("com.adsbynimbus.android:extension-okhttp:$nimbusVersion")
    implementation("com.adsbynimbus.android:extension-viewability:$nimbusVersion")
    implementation("com.adsbynimbus.android:extension-unity:$nimbusVersion")

    // Aps
    implementation("com.amazon.android:aps-sdk:9.5.1")

    // Facebook
    implementation("com.facebook.android:audience-network-sdk:6.11.0")

    // Google
    implementation("com.google.android.gms:play-services-ads:20.6.0")

    // Core, Fragment, AppCompat
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.fragment:fragment-ktx:1.4.1")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.4.2")
    implementation("androidx.navigation:navigation-runtime-ktx:2.4.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.4.2")

    // Preferences
    implementation("androidx.preference:preference-ktx:1.2.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // Material
    implementation("com.google.android.material:material:1.6.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    constraints {
        api("androidx.activity:activity:1.4.0")
        api("androidx.activity:activity-ktx:1.4.0")
        api("androidx.annotation:annotation:1.3.0")
        api("androidx.annotation:annotation-experimental:1.2.0")
        api("androidx.browser:browser:1.4.0")
        api("androidx.collection:collection:1.2.0")
        api("androidx.collection:collection-ktx:1.2.0")
        api("androidx.constraintlayout:constraintlayout:2.1.3")
        api("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
        api("androidx.lifecycle:lifecycle-livedata:2.4.1")
        api("androidx.lifecycle:lifecycle-runtime:2.4.1")
        api("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
        api("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
        api("androidx.media2:media2-player:1.2.1")
        api("androidx.media2:media2-widget:1.2.1")
        api("androidx.media:media:1.6.0")
        api("androidx.room:room-runtime:2.4.2")
        api("androidx.transition:transition:1.4.1")
        api("androidx.work:work-runtime:2.7.1")
        api("com.google.android.gms:play-services-ads-identifier:18.0.1")
        api("com.google.ads.interactivemedia.v3:interactivemedia:3.27.0")
        api("com.squareup.okio:okio:3.1.0")
        api("org.jetbrains:annotations:23.0.0")
    }
}
