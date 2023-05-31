package com.adsbynimbus.android.sample.demand

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adsbynimbus.Nimbus
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.EmptyAdControllerListenerImplementation
import com.adsbynimbus.android.sample.rendering.NimbusAdManagerTestListener
import com.adsbynimbus.android.sample.rendering.showPropertyMissingDialog
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.UnityDemandProvider

/** Initializes the Unity SDK and integrates it with the Nimbus SDK. */
fun Context.initializeUnity(unityGameId: String) {
    if (Nimbus.testMode) UnityDemandProvider.initializeTestMode(this, gameId = unityGameId) else {
        UnityDemandProvider.initialize(this, gameId = unityGameId)
    }
}

class UnityFragment : Fragment() {

    val adManager: NimbusAdManager = NimbusAdManager()

    fun NimbusRequest.removeNonUnityDemand() = apply {
        interceptors.add(NimbusRequest.Interceptor {
            request.imp[0].ext.facebook_app_id = null
            request.user?.ext = request.user?.ext?.apply {
                facebook_buyeruid = null
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
            "Unity Rewarded Video" -> adManager.showRewardedAd(
                request = NimbusRequest.forRewardedVideo(item).removeNonUnityDemand(),
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
}
