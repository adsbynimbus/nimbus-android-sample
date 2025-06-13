package com.adsbynimbus.android.sample

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.startup.Initializer
import com.adsbynimbus.Nimbus
import com.adsbynimbus.android.sample.demand.*
import com.adsbynimbus.android.sample.rendering.UiTestInterceptor
import com.adsbynimbus.render.Renderer
import com.adsbynimbus.request.OkHttpNimbusClient
import com.adsbynimbus.request.RequestManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

@Suppress("unused")
class NimbusInitializer : Initializer<Nimbus> {
    override fun create(context: Context): Nimbus {
        Nimbus.initialize(context, BuildConfig.PUBLISHER_KEY, BuildConfig.API_KEY)
        Nimbus.testMode = true

        /* The Timber.DebugTree proxies all calls to Timber.log to the logcat console */
        Timber.plant(Timber.DebugTree())

        /* Attaches a logger for SDK events that are sent to Timber */
        Nimbus.addLogger { level, message -> Timber.log(level, message) }

        val metaPlacementIds = listOf(
            BuildConfig.FAN_NATIVE_ID,
            BuildConfig.FAN_NATIVE_320_ID,
            BuildConfig.FAN_BANNER_320_ID,
            BuildConfig.FAN_INTERSTITIAL_ID,
        )

        /* Initialize additional Demand SDKs */
        with(context) {
            /* APS samples can be found in the Demand folder */
            initializeAmazonPublisherServices(appKey = BuildConfig.APS_APP_KEY)

            /* Initializes Meta Audience Network if any of the placement ids have been defined */
            metaPlacementIds.firstOrNull { it.isNotEmpty() }?.let {

                /* Meta samples can be found in the Demand folder */
                initializeMetaAudienceNetwork(appId = appIdFromMetaPlacementId(placement = it))
            }

            /* Unity samples can be found in the Demand folder */
            if (BuildConfig.UNITY_GAME_ID.isNotEmpty()) initializeUnity(unityGameId = BuildConfig.UNITY_GAME_ID)

            /* Vungle samples can be found in the Demand folder*/
            initializeVungle(vungleAppId = BuildConfig.VUNGLE_CONFIG_ID)
        }

        /* The following is used for the sample app only */
        if (Nimbus.testMode) configureSampleAppForTesting(context)

        return Nimbus
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()

    private fun configureSampleAppForTesting(context: Context) {
        /*
            The following line of code demonstrates how to change the endpoint the SDK points to
            for advanced use cases such as using a proxy server.
        */
        RequestManager.setRequestUrl("https://${BuildConfig.PUBLISHER_KEY}.adsbynimbus.com/rta/test")

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        RequestManager.setClient(
            OkHttpNimbusClient(
                OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor {
                    Timber.tag("Nimbus Request")
                    Timber.v(it)
                }.setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor {
                    if (preferences.forceAdRequestError) it.proceed(
                        it.request().newBuilder().addHeader("Nimbus-Test-No-Fill", "true").build()
                    ) else it.proceed(it.request())
                })
        )

        Renderer.interceptors.add(UiTestInterceptor)
    }
}
