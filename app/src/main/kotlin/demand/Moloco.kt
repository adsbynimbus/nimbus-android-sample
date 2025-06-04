package com.adsbynimbus.android.sample.demand

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.GoogleNativeAdBinding
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.mediation.removeOtherDemandIds
import com.adsbynimbus.android.sample.rendering.EmptyAdControllerListenerImplementation
import com.adsbynimbus.android.sample.rendering.NimbusAdManagerTestListener
import com.adsbynimbus.android.sample.rendering.align
import com.adsbynimbus.internal.nimbusScope
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.AdMobRenderer
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.RequestManager
import com.adsbynimbus.request.withAdMobBanner
import com.adsbynimbus.request.withAdMobInterstitial
import com.adsbynimbus.request.withAdMobNative
import com.adsbynimbus.request.withAdMobRewarded
import com.adsbynimbus.request.withMoloco
import com.google.android.gms.ads.nativead.NativeAd
import com.moloco.sdk.internal.MolocoLogger
import com.moloco.sdk.publisher.MediationInfo
import com.moloco.sdk.publisher.Moloco
import com.moloco.sdk.publisher.init.MolocoInitParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

fun initializeMoloco(context: Context, appKey: String, block: () -> Unit) {
    MolocoLogger.logEnabled = true

        Moloco.initialize(
            MolocoInitParams(
                appContext = context.applicationContext,
                appKey = appKey,
                mediationInfo = MediationInfo("none")
            )
        ) { result ->
            Timber.v("Moloco initialized with ${result.initialization} - ${result.description}")
            nimbusScope.launch(Dispatchers.Main) {
                block()
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
        when (val item = requireArguments().getString("item")) {
            "Banner" -> nimbusScope.launch(Dispatchers.IO) {
                initializeMoloco(requireContext(), BuildConfig.MOLOCO_APP_KEY) {
                    showBanner(item)
                }
            }
            "MREC" -> adManager.showAd(
                request = NimbusRequest.forBannerAd(item, Format.MREC, Position.HEADER).apply {
                    removeOtherDemandIds()
                    withAdMobBanner(BuildConfig.ADMOB_BANNER)
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
                    withAdMobInterstitial(BuildConfig.ADMOB_INTERSTITIAL)
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
                    withAdMobRewarded(BuildConfig.ADMOB_REWARDED)
                },
                activity = requireActivity(),
                closeButtonDelaySeconds = 60,
                listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                    /* Replace the following with your own AdController.Listener implementation */
                    controller.listeners.add(EmptyAdControllerListenerImplementation)
                },
            )
            "Native" -> {
                AdMobRenderer.delegate = object : AdMobRenderer.Delegate {
                    override fun customViewForRendering(container: ViewGroup, nativeAd: NativeAd): View {
                        return GoogleNativeAdBinding.inflate(LayoutInflater.from(container.context)).apply {
                            populateNativeAdView(nativeAd, this)
                        }.root
                    }
                }
                adManager.showAd(
                    request = NimbusRequest.forNativeAd(item).apply {
                        companionAds = emptyArray()
                        removeOtherDemandIds()
                        withAdMobNative(BuildConfig.ADMOB_NATIVE)
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

    private fun LayoutInlineAdBinding.showBanner(item: String) {
        adManager.showAd(
            request = NimbusRequest.forBannerAd(item, Format.BANNER_320_50, Position.HEADER).apply {
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

    private fun populateNativeAdView(nativeAd: NativeAd, unifiedAdBinding: GoogleNativeAdBinding) {
        val nativeAdView = unifiedAdBinding.root

        // Set the media view.
        nativeAdView.mediaView = unifiedAdBinding.adMedia

        // Set other ad assets.
        nativeAdView.headlineView = unifiedAdBinding.adHeadline
        nativeAdView.bodyView = unifiedAdBinding.adBody
        nativeAdView.callToActionView = unifiedAdBinding.adCallToAction
        nativeAdView.iconView = unifiedAdBinding.adAppIcon
        nativeAdView.priceView = unifiedAdBinding.adPrice
        nativeAdView.starRatingView = unifiedAdBinding.adStars
        nativeAdView.storeView = unifiedAdBinding.adStore
        nativeAdView.advertiserView = unifiedAdBinding.adAdvertiser

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        unifiedAdBinding.adHeadline.text = nativeAd.headline
        nativeAd.mediaContent?.let { unifiedAdBinding.adMedia.mediaContent = it }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            unifiedAdBinding.adBody.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adBody.visibility = View.VISIBLE
            unifiedAdBinding.adBody.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            unifiedAdBinding.adCallToAction.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adCallToAction.visibility = View.VISIBLE
            unifiedAdBinding.adCallToAction.text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            unifiedAdBinding.adAppIcon.visibility = View.GONE
        } else {
            unifiedAdBinding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
            unifiedAdBinding.adAppIcon.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            unifiedAdBinding.adPrice.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adPrice.visibility = View.VISIBLE
            unifiedAdBinding.adPrice.text = nativeAd.price
        }

        if (nativeAd.store == null) {
            unifiedAdBinding.adStore.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adStore.visibility = View.VISIBLE
            unifiedAdBinding.adStore.text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            unifiedAdBinding.adStars.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adStars.rating = nativeAd.starRating!!.toFloat()
            unifiedAdBinding.adStars.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            unifiedAdBinding.adAdvertiser.visibility = View.INVISIBLE
        } else {
            unifiedAdBinding.adAdvertiser.text = nativeAd.advertiser
            unifiedAdBinding.adAdvertiser.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        nativeAdView.setNativeAd(nativeAd)
    }
}

