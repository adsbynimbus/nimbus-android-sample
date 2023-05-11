package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.test.NimbusAdManagerTestListener
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.AdEvent
import com.adsbynimbus.render.Renderer
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.RequestManager
import com.adsbynimbus.request.VungleDemandProvider
import timber.log.Timber

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
        logContainer.visibility = View.VISIBLE
        when (val item = requireArguments().getString("item")) {
            "Vungle Banner" -> adManager.showAd(
                request = NimbusRequest.forBannerAd(item, Format.BANNER_320_50).also {
                    it.removeNonVungleDemand()
                },
                viewGroup = adFrame,
                listener = NimbusAdManagerTestListener(identifier = item) { controller ->
                    adController = controller
                },
            )
            "Vungle MREC" -> adManager.showAd(
                request = NimbusRequest.forBannerAd(item, Format.MREC).also {
                    it.removeNonVungleDemand()
                },
                viewGroup = adFrame,
                listener = NimbusAdManagerTestListener(identifier = item) { controller ->
                    adController = controller
                },
            )
            "Vungle Interstitial" -> adManager.showBlockingAd(
                request =  NimbusRequest.forInterstitialAd(item).also {
                    it.removeNonVungleDemand()
                },
                activity = requireActivity(),
                listener = NimbusAdManagerTestListener(identifier = item) { controller ->
                    adController = controller
                },
            )
            "Vungle Rewarded" -> adManager.showBlockingAd(
                request = NimbusRequest.forRewardedVideo(item).also {
                    it.removeNonVungleDemand()
                },
                activity = requireActivity(),
                listener = NimbusAdManagerTestListener(identifier = item) { controller ->
                    adController = controller
                },
            )
        }
    }.root

    override fun onDestroyView() {
        VungleDemandProvider.enabled = false
        adController?.destroy()
        adController = null
        super.onDestroyView()
    }
}

var VungleDemandProvider.enabled: Boolean
    get() = RequestManager.interceptors.contains(this)
    set(enabled) = with(RequestManager.interceptors) {
        if (enabled) add(VungleDemandProvider) else remove(VungleDemandProvider)
    }
