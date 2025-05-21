package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.view.*
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.databinding.LayoutMintegralNativeAdBinding
import com.adsbynimbus.android.sample.mediation.removeOtherDemandIds
import com.adsbynimbus.android.sample.rendering.*
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.MintegralRenderer
import com.adsbynimbus.request.*
import com.mbridge.msdk.out.Campaign

class MintegralFragment  : Fragment(), NimbusRequest.Interceptor {

    val adManager: NimbusAdManager = NimbusAdManager()
    val controllers = mutableListOf<AdController>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        when (val item = requireArguments().getString("item")) {
            "Banner" -> adManager.showAd(
                request = NimbusRequest.forBannerAd(item, Format.BANNER_320_50, Position.HEADER).apply {
                    removeOtherDemandIds()
                    withMintegral(
                        adUnitId = BuildConfig.MINTEGRAL_BANNER_ADUNIT,
                        placementId = BuildConfig.MINTEGRAL_BANNER_PLACEMENT
                    )
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
            "MREC" -> adManager.showAd(
                request = NimbusRequest.forBannerAd(item, Format.MREC, Position.HEADER).apply {
                    removeOtherDemandIds()
                    withMintegral(
                        adUnitId = BuildConfig.MINTEGRAL_BANNER_ADUNIT,
                        placementId = BuildConfig.MINTEGRAL_BANNER_PLACEMENT
                    )
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
                    removeOtherDemandIds()
                    withMintegral(
                        adUnitId = BuildConfig.MINTEGRAL_INTERSTITIAL_ADUNIT,
                        placementId = BuildConfig.MINTEGRAL_INTERSTITIAL_PLACEMENT
                    )
                },
                activity = requireActivity(),
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    /* Replace the following with your own AdController.Listener implementation */
                    controller.listeners.add(EmptyAdControllerListenerImplementation)
                },
            )
            "Rewarded" -> adManager.showRewardedAd(
                request = NimbusRequest.forRewardedVideo(item).apply {
                    companionAds = emptyArray()
                    removeOtherDemandIds()
                    withMintegral(
                        adUnitId = BuildConfig.MINTEGRAL_REWARDED_ADUNIT,
                        placementId = BuildConfig.MINTEGRAL_REWARDED_PLACEMENT
                    )
                },
                activity = requireActivity(),
                closeButtonDelaySeconds = 60,
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    /* Replace the following with your own AdController.Listener implementation */
                    controller.listeners.add(EmptyAdControllerListenerImplementation)
                },
            )
            "Native" -> {
                MintegralRenderer.delegate = object : MintegralRenderer.Delegate {
                    override fun customViewForRendering(
                        container: ViewGroup,
                        campaign: Campaign,
                    ): ViewGroup =
                        LayoutMintegralNativeAdBinding.inflate(LayoutInflater.from(container.context)).apply {
                            populateNativeAdView(campaign)
                        }.root
                }
                adManager.showAd(
                    request = NimbusRequest.forNativeAd(item, includeVideo = true).apply {
                        companionAds = emptyArray()
                        removeOtherDemandIds()
                        withMintegral(
                            adUnitId = BuildConfig.MINTEGRAL_NATIVE_ADUNIT,
                            placementId = BuildConfig.MINTEGRAL_NATIVE_PLACEMENT
                        )
                    },
                    viewGroup = adFrame,
                    listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                        /* Replace the following with your own AdController.Listener implementation */
                        controller.listeners.add(EmptyAdControllerListenerImplementation)
                    },
                )
            }
        }
    }.root

    private fun LayoutMintegralNativeAdBinding.populateNativeAdView(campaign: Campaign) {
        // resize ad choice accordingly
        customChoice.updateLayoutParams {
            width = campaign.adchoiceSizeWidth
            height = campaign.adchoiceSizeHeight
        }

        // initialize the campaign
        customChoice.setCampaign(campaign)
        customMedia.setNativeAd(campaign)

        // update other custom fields using info from campaign
        customTitle.text = campaign.appName
        customDesc.text = campaign.appDesc
    }

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
