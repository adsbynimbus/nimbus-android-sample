package com.adsbynimbus.android.sample.demand

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.Gravity.TOP
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.BuildConfig.MOLOCO_BANNER_ADUNITID
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.databinding.MolocoNativeAdBinding
import com.adsbynimbus.android.sample.mediation.removeOtherDemandIds
import com.adsbynimbus.android.sample.rendering.*
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.*
import com.adsbynimbus.request.*
import com.moloco.sdk.internal.MolocoLogger
import com.moloco.sdk.publisher.MediationInfo
import com.moloco.sdk.publisher.Moloco
import com.moloco.sdk.publisher.init.MolocoInitParams
import timber.log.Timber

fun initializeMoloco(context: Context, appKey: String) {
    if (!Moloco.isInitialized) {
        MolocoLogger.logEnabled = true
        Moloco.initialize(
            MolocoInitParams(
                appContext = context.applicationContext,
                appKey = appKey,
                mediationInfo = MediationInfo("none")
            )
        ) { result ->
            Timber.v("Moloco initialized with ${result.initialization} - ${result.description}")
        }
    }
}

class MolocoFragment : Fragment(), NimbusRequest.Interceptor {

    val adManager: NimbusAdManager = NimbusAdManager()
    val controllers = mutableListOf<AdController>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        initializeMoloco(requireContext(), BuildConfig.MOLOCO_APP_KEY)
        when (val item = requireArguments().getString("item")) {
            "Banner" -> adManager.showAd(
                request = NimbusRequest.forBannerAd(item, Format.BANNER_320_50, Position.HEADER).apply {
                    removeOtherDemandIds()
                    withMoloco(MOLOCO_BANNER_ADUNITID)
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
            "MREC" -> adManager.showAd(
                request = NimbusRequest.forBannerAd(item, Format.MREC, Position.HEADER).apply {
                    removeOtherDemandIds()
                    withMoloco(BuildConfig.MOLOCO_BANNER_ADUNITID)
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
                    withMoloco(BuildConfig.MOLOCO_INTERSTITIAL_ADUNITID)
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
                    withMoloco(BuildConfig.MOLOCO_REWARDED_ADUNITID)
                },
                activity = requireActivity(),
                closeButtonDelaySeconds = 60,
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    /* Replace the following with your own AdController.Listener implementation */
                    controller.listeners.add(EmptyAdControllerListenerImplementation)
                },
            )

            "Native" -> {
                MolocoRenderer.delegate = object : MolocoRenderer.Delegate {
                    override fun customViewForRendering(container: ViewGroup, nimbusMolocoNativeAd: NimbusMolocoNativeAd): View =
                        MolocoNativeAdBinding.inflate(LayoutInflater.from(container.context)).apply {
                            populateNativeAdView(nimbusMolocoNativeAd, this)
                        }.root
                }
                adManager.showAd(
                    request = NimbusRequest.forNativeAd(item).apply {
                        removeOtherDemandIds()
                        withMoloco(BuildConfig.MOLOCO_NATIVE_ADUNITID)
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

    private fun populateNativeAdView(nativeAd: NimbusMolocoNativeAd, binding: MolocoNativeAdBinding) = with(nativeAd) {
        assets.title?.let {
            binding.adHeadline.text = it
            binding.adHeadline.visibility = View.VISIBLE
        } ?: run { binding.adHeadline.visibility = View.INVISIBLE }

        assets.description?.let {
            binding.adBody.text = it
            binding.adBody.visibility = View.VISIBLE
        } ?: run { binding.adBody.visibility = View.INVISIBLE }

        assets.callToActionText?.let {
            binding.adCallToAction.text = it
            binding.adCallToAction.visibility = View.VISIBLE
        } ?: run { binding.adCallToAction.visibility = View.INVISIBLE }

        assets.sponsorText?.let {
            binding.adAdvertiser.text = it
            binding.adAdvertiser.visibility = View.VISIBLE
        } ?: run { binding.adAdvertiser.visibility = View.INVISIBLE }

        assets.rating?.let {
            binding.adStars.rating = it
            binding.adStars.visibility = View.VISIBLE
        } ?: run { binding.adStars.visibility = View.INVISIBLE }

        assets.mediaView?.let {
            binding.mediaContainer.addView(it)
        }

        assets.iconUri?.let {
            binding.adAppIcon.setImageURI(it)
            binding.adAppIcon.visibility = View.VISIBLE
        } ?: run { binding.adAppIcon.visibility = View.INVISIBLE }

        clickableViews.add(binding.adCallToAction)
        clickableViews.add(binding.adHeadline)
    }
}

