package com.adsbynimbus.android.sample.mediation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.LogAdapter
import com.adsbynimbus.android.sample.rendering.useAsLogger
import com.adsbynimbus.google.NimbusRewardCallback
import com.adsbynimbus.google.showAd
import com.adsbynimbus.lineitem.applyDynamicPrice
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.CompanionAd
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.NimbusRequest.Companion.forBannerAd
import com.adsbynimbus.request.NimbusRequest.Companion.forInterstitialAd
import com.adsbynimbus.request.NimbusRequest.Companion.forRewardedVideo
import com.adsbynimbus.request.NimbusRequest.Companion.forVideoAd
import com.google.android.gms.ads.*
import com.google.android.gms.ads.admanager.*
import com.google.android.gms.ads.rewarded.*
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.launch
import timber.log.Timber

const val unitId = "/182491903/nimbus-rendering-dynamic-price"
const val rewardedUnitId = "/182491903/nimbus_rendering_rewarded"

inline val LoadAdError.isNoFill: Boolean
    @SuppressLint("VisibleForTests")
    get() = code in intArrayOf(AdRequest.ERROR_CODE_NO_FILL, AdRequest.ERROR_CODE_MEDIATION_NO_FILL)

/**
 * @param auctionData Created after receiving a response from Nimbus
 */
fun AdManagerAdView.setNimbusListeners(
    logAdapter: LogAdapter,
) {
    val listener = object : AdListener(), AppEventListener, OnPaidEventListener {
        override fun onAdFailedToLoad(loadError: LoadAdError) {
            logAdapter.appendLog("onAdFailedToLoad $loadError")
        }

        override fun onAdImpression() {
            logAdapter.appendLog("onImpression")
        }

        override fun onPaidEvent(event: AdValue) {
            logAdapter.appendLog("onPaidEvent $event")
        }

        override fun onAdClicked() {
            logAdapter.appendLog("onAdClicked")
        }

        override fun onAdClosed() {
            logAdapter.appendLog("onAdClosed")
        }

        override fun onAdLoaded() {
            logAdapter.appendLog("onAdLoaded")
        }

        override fun onAdOpened() {
            logAdapter.appendLog("onAdOpened")
        }

        override fun onAppEvent(name: String, info: String) {
            logAdapter.appendLog("onAppEvent $name")
        }

    }
    adListener = listener
    appEventListener = listener
    onPaidEventListener = listener
}

fun AdManagerInterstitialAd.setNimbusListeners(
    logAdapter: LogAdapter,
) {
    val eventListener = object : FullScreenContentCallback(), AppEventListener, OnPaidEventListener {

        override fun onAdImpression() {
            logAdapter.appendLog("onImpression")
        }

        override fun onAppEvent(name: String, info: String) {
            logAdapter.appendLog("onAppEvent $name")
        }

        override fun onAdClicked() {
            logAdapter.appendLog("onAdClicked")
        }

        override fun onAdDismissedFullScreenContent() {
            logAdapter.appendLog("onAdDismissedFullScreenContent")
        }

        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
            logAdapter.appendLog("onAdFailedToShowFullScreenContent")
        }

        override fun onAdShowedFullScreenContent() {
            logAdapter.appendLog("onAdShowedFullScreenContent")
        }

        override fun onPaidEvent(p0: AdValue) {
            TODO("Not yet implemented")
        }
    }
    appEventListener = eventListener
    fullScreenContentCallback = eventListener
    onPaidEventListener = eventListener
}

class AdLoaderAdListener(
    val logAdapter: LogAdapter,
) : AdListener() {
    var responseInfo: ResponseInfo? = null
    override fun onAdFailedToLoad(loadError: LoadAdError) {
        logAdapter.appendLog("onAdFailedToLoad $loadError")
    }

    override fun onAdImpression() {
        logAdapter.appendLog("onImpression")
    }

    override fun onAdClicked() {
        logAdapter.appendLog("onAdClicked")
    }

    override fun onAdClosed() {
        logAdapter.appendLog("onAdClosed")
    }

    override fun onAdLoaded() {
        logAdapter.appendLog("onAdLoaded")
    }

    override fun onAdOpened() {
        logAdapter.appendLog("onAdOpened")
    }
}
class DynamicPriceFragment : Fragment() {

    val adManager = NimbusAdManager()
    var adView: AdManagerAdView? = null
    val logAdapter = LogAdapter()

    private var adController: AdController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        logs.useAsLogger(logAdapter)

        when (requireArguments().getString("item")) {
            "Banner" -> adFrame.addView(AdManagerAdView(root.context).apply {
                adUnitId = unitId
                setAdSize(AdSize.BANNER)
            }.also { adManagerAdView ->
                adView = adManagerAdView
                lifecycleScope.launch {
                    runCatching {
                        adManager.makeRequest(root.context, forBannerAd("test_dp_rendering", Format.BANNER_320_50))
                    }.onSuccess { nimbusAd ->
                        adManagerAdView.setNimbusListeners(logAdapter)
                        adManagerAdView.loadAd(AdManagerAdRequest.Builder().applyDynamicPrice(nimbusAd).build())
                    }
                }
            })

            "Inline Video" -> adFrame.addView(AdManagerAdView(root.context).apply {
                adUnitId = unitId
                setAdSize(AdSize(320, 480))
            }.also { adManagerAdView ->
                adView = adManagerAdView
                lifecycleScope.launch {
                    runCatching {
                        adManager.makeRequest(root.context, forVideoAd("test_dp_rendering").apply {
                            companionAds = arrayOf(CompanionAd.end(320, 480))
                        })
                    }.onSuccess { nimbusAd ->
                        adManagerAdView.setNimbusListeners(logAdapter)
                        adManagerAdView.loadAd(AdManagerAdRequest.Builder().applyDynamicPrice(nimbusAd).build())
                    }
                }
            })

            "Interstitial" -> lifecycleScope.launch {
                runCatching {
                    adManager.makeRequest(root.context, forInterstitialAd("test_dp_rendering"))
                }.onSuccess { nimbusAd ->
                    val adManagerRequest = AdManagerAdRequest.Builder().applyDynamicPrice(nimbusAd).build()
                    AdManagerInterstitialAd.load(requireContext(), unitId,
                        adManagerRequest, object : AdManagerInterstitialAdLoadCallback() {
                            override fun onAdLoaded(ad: AdManagerInterstitialAd) {
                                ad.setNimbusListeners(logAdapter)
                                ad.show(requireActivity())
                            }
                        })
                }
            }

            "Rewarded Video" -> lifecycleScope.launch {
                runCatching {
                    adManager.makeRequest(root.context, forRewardedVideo("test_dp_rendering").removeOtherDemandIds())
                }.onSuccess { nimbusAd ->
                    val adManagerRequest = AdManagerAdRequest.Builder().applyDynamicPrice(nimbusAd).build()
                    RewardedAd.load(requireActivity(), rewardedUnitId,
                        adManagerRequest, object : RewardedAdLoadCallback() {
                            override fun onAdLoaded(ad: RewardedAd) {

                                // When ready to show the ad, use
                                ad.showAd(requireActivity(), nimbusAd, adManager, LoggingRewardCallback())
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                Timber.e("Error loading rewarded video: $error")
                            }
                        })
                }
            }

            "Rewarded Interstitial" -> lifecycleScope.launch {
                runCatching {
                    adManager.makeRequest(root.context, forRewardedVideo("test_dp_rendering").removeOtherDemandIds())
                }.onSuccess { nimbusAd ->
                    val adManagerRequest = AdManagerAdRequest.Builder().applyDynamicPrice(nimbusAd).build()
                    RewardedInterstitialAd.load(requireActivity(), rewardedUnitId,
                        adManagerRequest, object : RewardedInterstitialAdLoadCallback() {
                            override fun onAdLoaded(ad: RewardedInterstitialAd) {

                                // When ready to show the ad, use
                                ad.showAd(requireActivity(), nimbusAd, adManager, LoggingRewardCallback())
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                Timber.e("Error loading rewarded video: $error")
                            }
                        })
                }
            }

            "Force Loss" -> adFrame.addView(AdManagerAdView(root.context).apply {
                adUnitId = unitId
                setAdSize(AdSize.BANNER)
            }.also { adManagerAdView ->
                adView = adManagerAdView
                lifecycleScope.launch {
                    runCatching {
                        adManager.makeRequest(root.context, forBannerAd("test_no_fill", Format.BANNER_320_50))
                    }.onSuccess { nimbusAd ->
                        adManagerAdView.setNimbusListeners(logAdapter)
                        adManagerAdView.loadAd(AdManagerAdRequest.Builder().applyDynamicPrice(nimbusAd).also {
                            it.addCustomTargeting("na_test", "no_fill")
                        }.build())
                    }
                }
            })

            "AdLoader Banner" -> {
                lifecycleScope.launch {
                    runCatching {
                        adManager.makeRequest(
                            root.context.applicationContext,
                            forBannerAd("test_dp_rendering", Format.MREC)
                        )
                    }.onSuccess { nimbusAd ->
                        val adLoaderAdListener = AdLoaderAdListener(logAdapter)
                        val builder = AdManagerAdRequest.Builder()
                        builder.applyDynamicPrice(nimbusAd)

                        AdLoader.Builder(root.context.applicationContext, unitId).forAdManagerAdView({
                            adView = it
                            it.layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            it.setAppEventListener { name, info ->
                                logAdapter.appendLog("onAppEvent $name")
                            }

                            it.setOnPaidEventListener { event ->
                                logAdapter.appendLog("onPaidEvent $event")
                            }
                            adLoaderAdListener.responseInfo = it.responseInfo
                            adFrame.addView(it)
                        }, AdSize.MEDIUM_RECTANGLE).withAdListener(adLoaderAdListener).build().loadAd(builder.build())
                    }
                }
            }

            "AdLoader Inline Video" -> {
                lifecycleScope.launch {
                    runCatching {
                        adManager.makeRequest(root.context.applicationContext, forVideoAd("test_dp_rendering"))
                    }.onSuccess { nimbusAd ->
                        val adLoaderAdListener = AdLoaderAdListener(logAdapter)
                        val builder = AdManagerAdRequest.Builder()
                        builder.applyDynamicPrice(nimbusAd)

                        AdLoader.Builder(root.context.applicationContext, unitId).forAdManagerAdView({
                            adView = it

                            it.setAppEventListener { name, info ->
                                logAdapter.appendLog("onAppEvent $name")
                            }

                            it.setOnPaidEventListener { event ->
                                logAdapter.appendLog("onPaidEvent $event")
                            }
                            adLoaderAdListener.responseInfo = it.responseInfo
                            adFrame.addView(it)
                        }, AdSize.MEDIUM_RECTANGLE).withAdListener(adLoaderAdListener).build().loadAd(builder.build())
                    }
                }
            }
        }
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        adView?.run {
            destroy()
            (parent as? ViewGroup)?.removeView(this)
        }
        adView = null
        adController?.destroy()
        adController = null
    }
}

class LoggingRewardCallback : NimbusRewardCallback {
    override fun onAdImpression() {
        Timber.v("On Rewarded ad impression")
    }

    override fun onAdClicked() {
        Timber.v("On Rewarded ad clicked")
    }

    override fun onAdPresented() {
        Timber.v("On Rewarded ad presented")
    }

    override fun onAdClosed() {
        Timber.v("On Rewarded ad closed")
    }

    override fun onUserEarnedReward(rewardItem: RewardItem) {
        Timber.v("On Rewarded ad reward earned ${rewardItem.type} - ${rewardItem.amount}")
    }

    override fun onError(nimbusError: NimbusError) {
        Timber.e(nimbusError, "On Rewarded ad error")
    }
}

fun NimbusRequest.removeOtherDemandIds() = also {
    interceptors.add { request ->
        request.request.imp[0].ext.facebook_app_id = null
        request.request.user?.ext = request.request.user?.ext?.apply {
            facebook_buyeruid = null
            unity_buyeruid = null
            vungle_buyeruid = null
            mfx_buyerdata = null
        }
    }
}
