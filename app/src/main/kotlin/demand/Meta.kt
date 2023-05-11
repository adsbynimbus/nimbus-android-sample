package com.adsbynimbus.android.sample.demand

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAd
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.test.LoggingAdControllerListener
import com.adsbynimbus.android.sample.test.NimbusAdManagerTestListener
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.Renderer
import com.adsbynimbus.render.Renderer.Companion.loadBlockingAd
import com.adsbynimbus.request.FANDemandProvider
import com.facebook.ads.AdSettings.TestAdType

fun Context.initializeMetaAudienceNetwork(appId: String) {
    FANDemandProvider.initialize(this, appId)
    //AdSettings.addTestDevice(/* Add Test Device ID From Logcat here if necessary */)
}

/** Returns the app id derived from a placement id */
fun appIdFromMetaPlacementId(placement: String) = placement.substringBefore("_")

/**
 * This Fragment shows what Facebook ads look like when run through the Nimbus renderer but is not
 * indicative of normal usage as the Nimbus server determines which ad units to request based on the
 * request sent from the client.
 */
class MetaFragment : Fragment() {

    val adManager: NimbusAdManager = NimbusAdManager()
    private var adController: AdController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        when (val item = requireArguments().getString("item")) {
            "Meta Banner",
            "Meta Native" -> Renderer.loadAd(
                ad = mockMetaNimbusAd(item),
                container = adFrame,
                listener = NimbusAdManagerTestListener(identifier = item) {
                    adController  = it.apply { listeners.add(LoggingAdControllerListener(identifier = item)) }
                })
            "Meta Interstitial" -> requireActivity().loadBlockingAd(mockMetaNimbusAd(item))?.also {
                adController = it.apply { listeners.add(LoggingAdControllerListener(identifier = item)) }
            }
        }
    }.root

    override fun onDestroyView() {
        adController?.destroy()
        adController = null
        super.onDestroyView()
    }
}

val bannerTypes = arrayOf(
    TestAdType.IMG_16_9_APP_INSTALL,
    TestAdType.IMG_16_9_LINK,
)
val interstitialTypes = bannerTypes + arrayOf(
    TestAdType.CAROUSEL_IMG_SQUARE_APP_INSTALL,
    TestAdType.CAROUSEL_IMG_SQUARE_LINK,
)
val nativeTypes = interstitialTypes

fun mockMetaNimbusAd(type: String) = object : NimbusAd {
    override fun placementId(): String = when(type) {
        "Meta Native" -> nativeTypes.random().let {
            it.adTypeString + "#" + if (it in bannerTypes) BuildConfig.FAN_NATIVE_320_ID else BuildConfig.FAN_NATIVE_ID
        }
        "Meta Interstitial" -> "${interstitialTypes.random().adTypeString}#${BuildConfig.FAN_INTERSTITIAL_ID}"
        else -> "${bannerTypes.random().adTypeString}#${BuildConfig.FAN_BANNER_320_ID}"
    }

    override fun type(): String = if (type == "Meta Native") "native" else "static"

    override fun network(): String = "facebook"

    override fun isInterstitial(): Boolean = type == "Meta Interstitial"

    override fun markup(): String = ""

    override fun width(): Int = if (type != "Meta Native") 320 else 0

    override fun height(): Int = when(type) {
        "Meta Banner" -> 50
        "Meta Interstitial" -> 480
        "Meta Native" -> 0
        else -> 0
    }
}
