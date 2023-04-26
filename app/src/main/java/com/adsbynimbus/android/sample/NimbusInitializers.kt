package com.adsbynimbus.android.sample

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.startup.Initializer
import com.adsbynimbus.Nimbus
import com.adsbynimbus.request.*
import com.amazon.device.ads.AdRegistration
import com.amazon.device.ads.DTBAdNetwork
import com.amazon.device.ads.DTBAdNetworkInfo
import com.amazon.device.ads.MRAIDPolicy
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

@Suppress("unused")
class NimbusInitializer : Initializer<Nimbus> {
    override fun create(context: Context): Nimbus = Nimbus.apply {
        initialize(context, BuildConfig.PUBLISHER_KEY, BuildConfig.API_KEY)
        testMode = true

        /* The Timber.DebugTree proxies all calls to Timber.log to the logcat console */
        Timber.plant(Timber.DebugTree())

        /*
            Attaches a logger for SDK events that are sent to Timber.
            This is the equivalent of calling Nimbus.addLogger()
         */
        addLogger { level, message -> Timber.log(level, message)}

        /*
            The following line of code demonstrates how to change the endpoint the SDK points to
            for advanced use cases such as using a proxy server.
         */
        RequestManager.setRequestUrl("https://dev-sdk.adsbynimbus.com/rta/test")

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        VungleDemandProvider.initialize(BuildConfig.VUNGLE_CONFIG_ID)

        RequestManager.setClient(OkHttpNimbusClient(OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor {
                Timber.tag("Nimbus Request")
                Timber.v(it)
            }.setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor {
                if (preferences.forceAdRequestError) it.proceed(
                    it.request().newBuilder().addHeader("Nimbus-Test-No-Fill", "true").build()
                ) else it.proceed(it.request())
            }))

        /* APS Initialization is at the end of this file */
        with(applicationContext) { initializeAPS(apiKey = BuildConfig.APS_APP_KEY) }

        val facebookAdUnitIds = listOf(
            BuildConfig.FAN_NATIVE_ID,
            BuildConfig.FAN_NATIVE_320_ID,
            BuildConfig.FAN_BANNER_320_ID,
            BuildConfig.FAN_INTERSTITIAL_ID,
        )
        /* Initializes the FANDemandProvider if any of the keys have been defined */
        facebookAdUnitIds.firstOrNull { it.isNotEmpty() }?.let {
            FANDemandProvider.initialize(context, it.substringBefore("_"))
            //AdSettings.addTestDevice(/* Add Test Device ID From Logcat here if necessary */)
        }

        /* Unity */
        if (BuildConfig.UNITY_GAME_ID.isNotEmpty()) {
            UnityDemandProvider.initializeTestMode(context, BuildConfig.UNITY_GAME_ID)
        }
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}

fun Context.initializeAPS(apiKey: String) {
    /* Initialize APS SDK */
    AdRegistration.getInstance(apiKey, this) // this is the application context

    /* Set the MRAID Policy */
    AdRegistration.setMRAIDSupportedVersions(arrayOf("1.0", "2.0", "3.0"))
    AdRegistration.setMRAIDPolicy(MRAIDPolicy.CUSTOM)

    /* Set Nimbus as the Mediator */
    AdRegistration.setAdNetworkInfo(DTBAdNetworkInfo(DTBAdNetwork.NIMBUS))

    /* Set Nimbus as the Open Measurement Partner */
    AdRegistration.addCustomAttribute("omidPartnerName", Nimbus.sdkName)
    AdRegistration.addCustomAttribute("omidPartnerVersion", Nimbus.version)

    /* Optional: Enable APS logging / test mode to verify the integration */
    AdRegistration.enableLogging(true)
    AdRegistration.enableTesting(true)
}
