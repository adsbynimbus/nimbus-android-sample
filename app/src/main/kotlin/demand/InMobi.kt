package com.adsbynimbus.android.sample.demand

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.Gravity.TOP
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.BuildConfig.*
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.mediation.removeOtherDemandIds
import com.adsbynimbus.android.sample.rendering.*
import com.adsbynimbus.openrtb.enumerations.Position.HEADER
import com.adsbynimbus.openrtb.request.Format.Companion.BANNER_320_50
import com.adsbynimbus.render.AdController
import com.adsbynimbus.request.*
import com.inmobi.sdk.InMobiSdk
import com.inmobi.sdk.SdkInitializationListener
import okhttp3.internal.toLongOrDefault
import org.json.JSONObject
import timber.log.Timber


fun initializeInMobi(context: Context, accountId: String) {
    if (InMobiSdk.isSDKInitialized()) return

    InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG)
    val consentObject = JSONObject()
    // Provide correct consent value to sdk which is obtained by User
    consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true)
    // Provide 0 if GDPR is not applicable and 1 if applicable
    consentObject.put("gdpr", "0")
    // Provide user consent in IAB format
//        consentObject.put(InMobiSdk.IM_GDPR_CONSENT_IAB, )

    InMobiSdk.init(context, accountId, consentObject, object : SdkInitializationListener {
        override fun onInitializationComplete(error: Error?) {
            Timber.w("InMobi SDK initialized with ${error?.message}")
        }
    })
}

class InMobiFragment : Fragment(), NimbusRequest.Interceptor {

    val adManager: NimbusAdManager = NimbusAdManager()
    val controllers = mutableListOf<AdController>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        initializeInMobi(requireContext(), INMOBI_ACCOUNT_ID)
        when (val item = requireArguments().getString("item")) {
            "Banner" -> adManager.showAd(
                request = NimbusRequest.forBannerAd(item, BANNER_320_50, HEADER).apply {
                    removeOtherDemandIds()
                    withInMobi(INMOBI_BANNER_PLACEMENT_ID.toLongOrDefault(0))
                },
                viewGroup = adFrame,
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    controllers.add(controller.apply {
                        align { TOP or CENTER_HORIZONTAL }
                        /* Replace the following with your own AdController.Listener implementation */
                        listeners.add(EmptyAdControllerListenerImplementation)
                    })
                },
            )
            "Interstitial" -> adManager.showBlockingAd(
                request = NimbusRequest.forInterstitialAd(item).apply {
                    removeOtherDemandIds()
                    withInMobi(INMOBI_INTERSTITIAL_PLACEMENT_ID.toLongOrDefault(0))
                },
                activity = requireActivity(),
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    /* Replace the following with your own AdController.Listener implementation */
                    controller.listeners.add(EmptyAdControllerListenerImplementation)
                },
            )
            "Rewarded" -> adManager.showRewardedAd(
                request = NimbusRequest.forRewardedVideo(item).apply {
                    removeOtherDemandIds()
                    withInMobi(INMOBI_REWARDED_PLACEMENT_ID.toLongOrDefault(0))
                },
                activity = requireActivity(),
                closeButtonDelaySeconds = 60,
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    /* Replace the following with your own AdController.Listener implementation */
                    controller.listeners.add(EmptyAdControllerListenerImplementation)
                },
            )
        }
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        RequestManager.interceptors.remove(this)
        controllers.forEach { it.destroy() }
    }

    override fun modifyRequest(request: NimbusRequest) {
        request.request.imp[0].ext.facebook_app_id = null
        request.request.user?.ext = request.request.user?.ext?.apply {
            facebook_buyeruid = null
            unity_buyeruid = null
            vungle_buyeruid = null
        }
    }
}
