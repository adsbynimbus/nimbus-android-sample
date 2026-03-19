package com.adsbynimbus.android.sample.demand

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.Gravity.TOP
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.ScreenAdLogger
import com.adsbynimbus.android.sample.rendering.disableAllExtensions
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.request.APSFetcher
import com.adsbynimbus.request.aps
import com.amazon.device.ads.*
import kotlinx.coroutines.launch

/** Demonstrates how to initialize the APS SDK for Nimbus */
fun Context.initializeAmazonPublisherServices(appKey: String) {
    /* Initialize APS SDK */
    AdRegistration.getInstance(appKey, this) // this is the application context

    /* Set the MRAID Policy */
    AdRegistration.setMRAIDSupportedVersions(arrayOf("1.0", "2.0", "3.0"))
    AdRegistration.setMRAIDPolicy(MRAIDPolicy.CUSTOM)

    /* Set Nimbus as the Open Measurement Partner */
    AdRegistration.addCustomAttribute("omidPartnerName", Nimbus.sdkName)
    AdRegistration.addCustomAttribute("omidPartnerVersion", Nimbus.version)

    /* Optional: Enable APS logging / test mode to verify the integration */
    AdRegistration.enableLogging(true)
    AdRegistration.enableTesting(true)
}

class APSFragment : Fragment() {

    private var ad: Ad? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        disableAllExtensions()
        when (val item = requireArguments().getString("item")) {
            "APS Banner With Refresh" -> lifecycleScope.launch {
                val apsRequest = DTBAdRequest(DTBAdNetworkInfo(DTBAdNetwork.NIMBUS)).apply {
                    setSizes(DTBAdSize(320, 50, BuildConfig.APS_BANNER))
                }

                /* See com.adsbynimbus.android.sample.request.Amazon.kt for implementation */

                val logger = ScreenAdLogger(identifier = item, logView = logs)
                val params = APSFetcher(apsRequest).fetchAds()

                ad = Nimbus.bannerAd(item, Format.BANNER_320_50, refreshInterval = 30) {
                    demand {
                        aps(params, listOf(apsRequest))
                    }
                }.onEvent {
                    logger.onAdEvent(it)
                }.onError {
                    logger.onError(it)
                }.show(adFrame).also {
                    it.adView?.updateLayoutParams<FrameLayout.LayoutParams> {
                        gravity = TOP or CENTER_HORIZONTAL
                        height = WRAP_CONTENT
                    }
                }
            }
            "APS Interstitial Hybrid" -> lifecycleScope.launch {
                val apsInterstitial = DTBAdRequest(DTBAdNetworkInfo(DTBAdNetwork.NIMBUS)).apply {
                    setSizes(DTBAdSize.DTBInterstitialAdSize(BuildConfig.APS_STATIC))
                }
                val apsVideo = DTBAdRequest(DTBAdNetworkInfo(DTBAdNetwork.NIMBUS)).apply {
                    setSizes(DTBAdSize.DTBVideo(
                        resources.displayMetrics.widthPixels,
                        resources.displayMetrics.heightPixels, BuildConfig.APS_VIDEO))
                }

                val logger = ScreenAdLogger(identifier = item, logView = logs)
                val params = APSFetcher(apsInterstitial, apsVideo).fetchAds()

                ad = Nimbus.interstitialAd(item) {
                    demand {
                        aps(params, listOf(apsVideo, apsInterstitial))
                    }
                }.onEvent {
                    logger.onAdEvent(it)
                }.onError {
                    logger.onError(it)
                }.show(from = this@APSFragment)
            }
        }
    }.root

    override fun onDestroyView() {
        ad?.destroy()
        ad = null
        super.onDestroyView()
    }
}
