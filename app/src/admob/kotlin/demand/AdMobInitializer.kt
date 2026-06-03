package com.adsbynimbus.android.sample.demand

import android.content.Context
import android.util.Log
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.internal.log
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig

object AdMobInitializer {
    fun initialize(context: Context) {
        val initConfig = InitializationConfig.Builder(BuildConfig.APPLICATION_ID).build()

        MobileAds.initialize(context, initConfig) {
            MobileAds.setRequestConfiguration(RequestConfiguration.Builder().setTestDeviceIds(listOf("28673F9AE3062465E9B3DCBD9575470F")).build())
            log( Log.VERBOSE, "GMA Next gen initialized with $it")
        }
    }
}
