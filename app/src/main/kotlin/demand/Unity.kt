package com.adsbynimbus.android.sample.demand

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.adsbynimbus.Nimbus
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.*
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.request.*

/** Initializes the Unity SDK and integrates it with the Nimbus SDK. */
fun Context.initializeUnity(unityGameId: String) {
    if (Nimbus.testMode) UnityDemandProvider.initializeTestMode(this, gameId = unityGameId) else {
        UnityDemandProvider.initialize(this, gameId = unityGameId)
    }
}

class UnityFragment : Fragment() {

    val adManager: NimbusAdManager = NimbusAdManager()
    val controllers = mutableListOf<AdController>()

    fun NimbusRequest.removeNonUnityDemand() = apply {
        interceptors.add(NimbusRequest.Interceptor {
            request.imp[0].ext.facebook_app_id = null
            request.user?.ext = request.user?.ext?.apply {
                facebook_buyeruid = null
                mfx_buyerdata = null
                vungle_buyeruid = null
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        when (val item = requireArguments().getString("item")) {
            "Banner" -> adManager.showAd(
                request = NimbusRequest.forBannerAd(item, Format.BANNER_320_50, Position.HEADER).apply {
                    removeNonUnityDemand()
                    withUnityBanner()
                },
                viewGroup = adFrame,
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    controllers.add(controller.apply {
                        align { Gravity.TOP or Gravity.CENTER_HORIZONTAL }
                        /* Replace the following with your own AdController.Listener implementation */
                        listeners.add(EmptyAdControllerListenerImplementation)
                    })
                },
            )
            "Interstitial" -> adManager.showBlockingAd(
                request = NimbusRequest.forInterstitialAd(item).apply {
                    removeNonUnityDemand()
                    withUnityInterstitial()
                },
                activity = requireActivity(),
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    /* Replace the following with your own AdController.Listener implementation */
                    controller.listeners.add(EmptyAdControllerListenerImplementation)
                },
            )
            "Rewarded Video" -> adManager.showRewardedAd(
                request = NimbusRequest.forRewardedVideo(item).removeNonUnityDemand()
                    .withUnityRewarded(),
                closeButtonDelaySeconds = 30,
                activity = requireActivity(),
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    /* Replace the following with your own AdController.Listener implementation */
                    controller.listeners.add(EmptyAdControllerListenerImplementation)
                }
            )
        }
        if (BuildConfig.UNITY_GAME_ID.isEmpty()) requireContext().showPropertyMissingDialog("sample_unity_game_id")
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        controllers.forEach { it.destroy() }
    }
}
