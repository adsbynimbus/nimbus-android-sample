package com.adsbynimbus.android.sample.demand

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.EmptyAdControllerListenerImplementation
import com.adsbynimbus.android.sample.rendering.LogAdapter
import com.adsbynimbus.android.sample.rendering.NimbusAdManagerTestListener
import com.adsbynimbus.android.sample.rendering.OnScreenLogger
import com.adsbynimbus.android.sample.rendering.showPropertyMissingDialog
import com.adsbynimbus.android.sample.rendering.useAsLogger
import com.adsbynimbus.openrtb.response.BidResponse
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.Renderer
import com.adsbynimbus.render.Renderer.Companion.loadBlockingAd
import com.adsbynimbus.request.FANDemandProvider
import com.adsbynimbus.request.NimbusResponse
import com.facebook.ads.AdSettings.TestAdType
import java.util.UUID

/**
 * Initializes the Meta SDK and integrates it with the Nimbus SDK.
 *
 * @param appId the application id provided by Meta; can be derived from a placement id
 * @see appIdFromMetaPlacementId
 */
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
            "Meta Native" -> mockMetaNimbusAd(item) { root.context.showPropertyMissingDialog(it) }.let { metaAd ->
                Renderer.loadAd(
                    ad = metaAd,
                    container = adFrame,
                    listener = NimbusAdManagerTestListener(identifier = item, logView = logs, response = metaAd) {
                        adController  = it.apply { listeners.add(EmptyAdControllerListenerImplementation) }
                    })
            }
            "Meta Interstitial" -> requireActivity().run {
                val metaAd = mockMetaNimbusAd(item) { showPropertyMissingDialog(it) }
                loadBlockingAd(metaAd)?.apply {
                    /* Replace the following with your own AdController.Listener implementation */
                    logs.useAsLogger(LogAdapter().also {
                        listeners.add(OnScreenLogger(it, response = metaAd))
                    })
                }?.start()
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

/** Creates a mock NimbusAd that can be sent to the Nimbus Renderer to load a test Meta ad */
fun mockMetaNimbusAd(type: String, onPropertyMissing: (String) -> Unit = {}) = NimbusResponse(bid = BidResponse(
    auction_id = UUID.randomUUID().toString(),
    placement_id = when(type) {
        "Meta Native" -> nativeTypes.random().let {
            it.adTypeString + "#" + if (it in bannerTypes) BuildConfig.FAN_NATIVE_320_ID.also { id ->
                if (id.isEmpty()) onPropertyMissing("sample_fan_native_320_id")
            } else BuildConfig.FAN_NATIVE_ID.also { id ->
                if (id.isEmpty()) onPropertyMissing("sample_fan_native_id")
            }
        }
        "Meta Interstitial" -> "${interstitialTypes.random().adTypeString}#${BuildConfig.FAN_INTERSTITIAL_ID.also { id ->
            if (id.isEmpty()) onPropertyMissing("sample_fan_interstitial_id")
        }}}"
        else -> "${bannerTypes.random().adTypeString}#${BuildConfig.FAN_BANNER_320_ID.also { id ->
            if (id.isEmpty()) onPropertyMissing("sample_fan_banner_320_id")
        }}"
    },
    markup = "",
    position = type,
    network = "facebook",  /** Nimbus currently refers to Meta demand as facebook under the hood */
    type = if (type == "Meta Native") "native" else "static",
    is_interstitial = if (type == "Meta Interstitial") 1 else 0,
    width = if (type != "Meta Native") 320 else 0,
    height = when(type) {
        "Meta Banner" -> 50
        "Meta Interstitial" -> 480
        "Meta Native" -> 0
        else -> 0
    }
))
