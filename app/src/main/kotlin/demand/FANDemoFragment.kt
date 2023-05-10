package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAd
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.AdEvent
import com.adsbynimbus.render.Renderer
import com.adsbynimbus.render.Renderer.Companion.loadBlockingAd
import com.facebook.ads.AdSettings.TestAdType
import timber.log.Timber

/**
 * This Fragment shows what Facebook ads look like when run through the Nimbus renderer but is not
 * indicative of normal usage as the Nimbus server determines which ad units to request based on the
 * request sent from the client.
 */
class FANDemoFragment : Fragment(), Renderer.Listener, AdController.Listener {

    private var adController: AdController? = null
    lateinit var item: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        when (requireArguments().getString("item", "").also { item = it }) {
            "Meta Banner",
            "Meta Native" -> Renderer.loadAd(mockMetaNimbusAd(item), adFrame, this@FANDemoFragment)
            "Meta Interstitial" -> requireActivity().loadBlockingAd(mockMetaNimbusAd(item))?.also {
                adController = it.apply { listeners.add(this@FANDemoFragment) }
            }
        }
    }.root

    override fun onDestroyView() {
        adController?.destroy()
        adController = null
        super.onDestroyView()
    }

    override fun onAdEvent(adEvent: AdEvent) {
        Timber.i("$item: %s", adEvent.name)
    }

    override fun onAdRendered(controller: AdController) {
        adController = controller.also {
            it.listeners.add(this)
            it.start()
        }
    }

    override fun onError(error: NimbusError) {
        Timber.e("$item: %s", error.message)
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
        else -> "${bannerTypes.random().adTypeString}#${BuildConfig.FAN_INTERSTITIAL_ID}"
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
