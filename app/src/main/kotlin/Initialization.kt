package com.adsbynimbus.android.sample

import android.content.Context
import androidx.startup.Initializer
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.demand.appIdFromMetaPlacementId
import com.adsbynimbus.android.sample.demand.initializeAmazonPublisherServices
import com.adsbynimbus.android.sample.rendering.UiTestInterceptor
import com.adsbynimbus.render.internal.Renderer
import com.inmobi.sdk.InMobiSdk
import timber.log.Timber

@Suppress("unused")
class NimbusInitializer : Initializer<Nimbus> {
    override fun create(context: Context): Nimbus {
        Nimbus.configuration.testMode = true

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

        Nimbus.initialize(context, BuildConfig.PUBLISHER_KEY, BuildConfig.API_KEY) {
            /* Initialize additional Demand SDKs */

            /* AdMob samples can be found in the Demand folder */
            if (BuildConfig.ADMOB_APPID.isNotEmpty()) {
                AdMobExtension()
            }

            /* APS samples can be found in the Demand folder */
            if (BuildConfig.APS_APP_KEY.isNotEmpty()) {
                context.initializeAmazonPublisherServices(appKey = BuildConfig.APS_APP_KEY)
            }

            /* InMobi samples can be found in the Demand Folder */
            if (BuildConfig.INMOBI_ACCOUNT_ID.isNotEmpty()) {
                InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG)
                InMobiExtension(accountId = BuildConfig.INMOBI_ACCOUNT_ID)
            }

            /* Initializes Meta Audience Network if any of the placement ids have been defined */
            metaPlacementIds.firstOrNull { it.isNotEmpty() }?.let {
                /* Meta samples can be found in the Demand folder */
                MetaExtension(metaAppId = appIdFromMetaPlacementId(placement = it))
                //AdSettings.addTestDevice(/* Add Test Device ID From Logcat here if necessary */)
            }

            /* Mintegral samples can be found in the Demand folder */
            if (BuildConfig.MINTEGRAL_APP_ID.isNotEmpty()) {
                MintegralExtension(BuildConfig.MINTEGRAL_APP_ID, BuildConfig.MINTEGRAL_APP_KEY)
            }

            /* MobileFuse samples can be found in the Demand folder */
            MobileFuseExtension()

            /* Moloco samples can be found in the Demand folder */
            if (BuildConfig.MOLOCO_APP_KEY.isNotEmpty()) {
                MolocoExtension(BuildConfig.MOLOCO_APP_KEY)
            }

            /* Unity samples can be found in the Demand folder */
            if (BuildConfig.UNITY_GAME_ID.isNotEmpty()) {
                UnityExtension(gameId = BuildConfig.UNITY_GAME_ID, testMode = Nimbus.configuration.testMode)
            }

            /* Vungle samples can be found in the Demand folder */
            if (BuildConfig.VUNGLE_CONFIG_ID.isNotEmpty()) {
                VungleExtension(appId = BuildConfig.VUNGLE_CONFIG_ID)
            }
        }

        /* The following is used for the sample app only */
        if (Nimbus.configuration.testMode) configureSampleAppForTesting(context)

        return Nimbus
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()

    private fun configureSampleAppForTesting(context: Context) {
        /*
            The following line of code demonstrates how to change the endpoint the SDK points to
            for advanced use cases such as using a proxy server.
        */
        Nimbus.configuration.requestUrl = "https://${BuildConfig.PUBLISHER_KEY}.adsbynimbus.com/rta/test"

        Renderer.interceptors.add(UiTestInterceptor)
    }
}
