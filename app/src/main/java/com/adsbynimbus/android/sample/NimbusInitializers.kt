package com.adsbynimbus.android.sample

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.startup.Initializer
import com.adsbynimbus.Nimbus
import com.adsbynimbus.request.*
import com.amazon.device.ads.AdRegistration
import com.amazon.device.ads.DTBAdSize
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


        /* APS Demand Provider */
        val apsAdUnits = mutableListOf<DTBAdSize>().apply {
            if (BuildConfig.APS_BANNER.isNotEmpty()) {
                add(DTBAdSize(320, 50, BuildConfig.APS_BANNER))
            }
            if (BuildConfig.APS_STATIC.isNotEmpty()) {
                add(DTBAdSize.DTBInterstitialAdSize(BuildConfig.APS_STATIC))
            }
            if (BuildConfig.APS_VIDEO.isNotEmpty()) with(context.resources.displayMetrics) {
                add(DTBAdSize.DTBVideo(widthPixels, heightPixels, BuildConfig.APS_VIDEO))
            }
        }
        if (apsAdUnits.isNotEmpty()) {
            /* The following initialize function is a vararg function unwrapping the above list */
            ApsDemandProvider.initialize(
                context,
                BuildConfig.APS_APP_KEY,
                *apsAdUnits.toTypedArray()
            )
            AdRegistration.enableTesting(true)
        }

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
