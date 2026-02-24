package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.adsbynimbus.Nimbus
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.*
import com.adsbynimbus.internal.ThirdPartyDemandNetwork
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.VungleRenderer
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.VungleDemandProvider
import com.vungle.ads.NativeAd
import com.vungle.ads.internal.ui.view.MediaView

/** Initializes the Vungle SDK and integrates it with the Nimbus SDK. */
fun initializeVungle(vungleAppId: String) {
    VungleDemandProvider.initialize(appId = vungleAppId)
    /** Disable Vungle demand until we are on the screen we want to show Vungle test ads */
    Nimbus.toggleDemand(false, ThirdPartyDemandNetwork.Vungle)
}

class VungleFragment : Fragment() {

    val adManager: NimbusAdManager = NimbusAdManager()
    var adController: AdController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        Nimbus.toggleDemand(true, ThirdPartyDemandNetwork.Vungle)
        VungleRenderer.delegate = NativeRenderingDelegate()
        when (val item = requireArguments().getString("item")) {
            "Vungle Banner" -> adManager.showAd(
                request = NimbusRequest.forBannerAd(item, Format.BANNER_320_50),
                viewGroup = adFrame,
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    adController = controller.apply {
                        align { Gravity.TOP or Gravity.CENTER_HORIZONTAL }
                        /* Replace the following with your own AdController.Listener implementation */
                        listeners.add(EmptyAdControllerListenerImplementation)
                    }
                },
            )
            "Vungle MREC" -> adManager.showAd(
                request = NimbusRequest.forBannerAd(item, Format.MREC),
                viewGroup = adFrame,
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    adController = controller.apply {
                        align { Gravity.TOP or Gravity.CENTER_HORIZONTAL }
                        /* Replace the following with your own AdController.Listener implementation */
                        listeners.add(EmptyAdControllerListenerImplementation)
                    }
                },
            )
            "Vungle Native Banner" -> adManager.showAd(
                request = NimbusRequest.forNativeAd(item),
                viewGroup = adFrame,
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    adController = controller.apply {
                        align { Gravity.TOP or Gravity.CENTER_HORIZONTAL }
                        /* Replace the following with your own AdController.Listener implementation */
                        listeners.add(EmptyAdControllerListenerImplementation)
                    }
                },
            )
            "Vungle Native Video" -> adManager.showAd(
                request = NimbusRequest.forNativeAd(item, includeVideo = true),
                viewGroup = adFrame,
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    adController = controller.apply {
                        align { Gravity.TOP or Gravity.CENTER_HORIZONTAL }
                        /* Replace the following with your own AdController.Listener implementation */
                        listeners.add(EmptyAdControllerListenerImplementation)
                    }
                },
            )
            "Vungle Interstitial" -> adManager.showBlockingAd(
                request =  NimbusRequest.forInterstitialAd(item),
                activity = requireActivity(),
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    adController = controller.apply {
                        /* Replace the following with your own AdController.Listener implementation */
                        listeners.add(EmptyAdControllerListenerImplementation)
                    }
                },
            )
            "Vungle Rewarded" -> adManager.showBlockingAd(
                request = NimbusRequest.forRewardedVideo(item),
                activity = requireActivity(),
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    adController = controller.apply {
                        /* Replace the following with your own AdController.Listener implementation */
                        listeners.add(EmptyAdControllerListenerImplementation)
                    }
                },
            )
        }
        if (BuildConfig.VUNGLE_CONFIG_ID.isEmpty()) context?.showPropertyMissingDialog("sample_vungle_config_id")
    }.root

    override fun onDestroyView() {
        adController?.destroy()
        adController = null
        super.onDestroyView()
    }
}

class NativeRenderingDelegate : VungleRenderer.Delegate {
    override fun customViewForRendering(container: ViewGroup, nativeAd: NativeAd): View = LayoutInflater.from(container.context)
        .inflate(R.layout.vungle_native_ad, container, false).apply {
            val nativeAdIcon = findViewById<ImageView>(R.id.native_ad_icon)
            val nativeAdTitle = findViewById<TextView>(R.id.native_ad_title)
            val nativeAdMedia = findViewById<MediaView>(R.id.native_ad_media)
            val rateView = findViewById<TextView>(R.id.rateTV)
            val nativeAdBody = findViewById<TextView>(R.id.native_ad_body)
            val sponsoredLabel = findViewById<TextView>(R.id.native_ad_sponsored_label)
            val nativeAdCallToAction = findViewById<Button>(R.id.native_ad_call_to_action)

            nativeAdTitle.text = nativeAd.getAdTitle()
            nativeAdBody.text = nativeAd.getAdBodyText()
            nativeAd.getAdStarRating()?.let { rateView.text = it.toString() }
            nativeAdCallToAction.visibility =
                if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
            nativeAdCallToAction.text = nativeAd.getAdCallToActionText()
            sponsoredLabel.text = nativeAd.getAdSponsoredText()

            nativeAd.registerViewForInteraction(
                this as FrameLayout, nativeAdMedia, nativeAdIcon, listOf(nativeAdTitle, nativeAdCallToAction)
            )
        }
}
