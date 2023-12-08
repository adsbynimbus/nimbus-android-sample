package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.EmptyAdControllerListenerImplementation
import com.adsbynimbus.android.sample.rendering.NimbusAdManagerTestListener
import com.adsbynimbus.android.sample.rendering.align
import com.adsbynimbus.android.sample.rendering.showPropertyMissingDialog
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.VungleRenderer
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.RequestManager
import com.adsbynimbus.request.VungleDemandProvider
import com.vungle.warren.NativeAd
import com.vungle.warren.NativeAdLayout
import com.vungle.warren.ui.view.MediaView

/** Initializes the Vungle SDK and integrates it with the Nimbus SDK. */
fun initializeVungle(vungleAppId: String) {
    VungleDemandProvider.initialize(appId = vungleAppId)
    /** Disable Vungle demand until we are on the screen we want to show Vungle test ads */
    VungleDemandProvider.enabled = false
}

class VungleFragment : Fragment() {

    val adManager: NimbusAdManager = NimbusAdManager()
    var adController: AdController? = null

    fun NimbusRequest.removeNonVungleDemand() = apply {
        interceptors.add(NimbusRequest.Interceptor {
            request.imp[0].ext.facebook_app_id = null
            request.user?.ext = request.user?.ext?.apply {
                facebook_buyeruid = null
                unity_buyeruid = null
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        VungleDemandProvider.enabled = true
        VungleRenderer.delegate = NativeRenderingDelegate()
        when (val item = requireArguments().getString("item")) {
            "Vungle Banner" -> adManager.showAd(
                request = NimbusRequest.forBannerAd(item, Format.BANNER_320_50).apply { removeNonVungleDemand() },
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
                request = NimbusRequest.forBannerAd(item, Format.MREC).apply { removeNonVungleDemand() },
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
                request = NimbusRequest.forNativeAd(item).apply { removeNonVungleDemand() },
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
                request = NimbusRequest.forNativeAd(item, includeVideo = true).apply { removeNonVungleDemand() },
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
                request =  NimbusRequest.forInterstitialAd(item).apply { removeNonVungleDemand() },
                activity = requireActivity(),
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    adController = controller.apply {
                        /* Replace the following with your own AdController.Listener implementation */
                        listeners.add(EmptyAdControllerListenerImplementation)
                    }
                },
            )
            "Vungle Rewarded" -> adManager.showBlockingAd(
                request = NimbusRequest.forRewardedVideo(item).apply { removeNonVungleDemand() },
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
        VungleDemandProvider.enabled = false
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

            nativeAdTitle.text = nativeAd.adTitle
            nativeAdBody.text = nativeAd.adBodyText
            nativeAd.adStarRating?.let { rateView.text = it.toString() }
            nativeAdCallToAction.visibility =
                if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
            nativeAdCallToAction.text = nativeAd.adCallToActionText
            sponsoredLabel.text = nativeAd.adSponsoredText

            nativeAd.registerViewForInteraction(
                this as NativeAdLayout, nativeAdMedia, nativeAdIcon, listOf(nativeAdTitle, nativeAdCallToAction)
            )
        }
}

var VungleDemandProvider.enabled: Boolean
    get() = RequestManager.interceptors.contains(this)
    set(enabled) = with(RequestManager.interceptors) {
        if (enabled) add(VungleDemandProvider) else remove(VungleDemandProvider)
    }
