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
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
                        aps(params, setOf(apsRequest))
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
                        aps(params, setOf(apsVideo, apsInterstitial))
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

/** Wraps an [AdError] response from a [DTBAdCallback] */
class DTBException(val error: AdError) : Exception() {
    inline val isNoBid: Boolean get() = error.code == AdError.ErrorCode.NO_FILL
    inline val isNetworkError: Boolean get() = error.code == AdError.ErrorCode.NETWORK_ERROR
    inline val isRequestError: Boolean get() = error.code == AdError.ErrorCode.REQUEST_ERROR
    inline val isUnknownError: Boolean get() = error.code == AdError.ErrorCode.INTERNAL_ERROR
}

/**
 * A cancellable coroutine that wraps [DTBAdRequest.loadAd].
 *
 * @param ttl The timeout in ms the loadAd call must return by
 * @return A bid from Amazon that can be appended directly to a NimbusRequest
 * @throws DTBException Amazon did not bid or an error occurred during the request
 * @throws TimeoutCancellationException Amazon did not return a response in time
 */
suspend fun DTBAdRequest.loadAd(ttl: Long = 1500): DTBAdResponse = withTimeout(ttl) {
    suspendCancellableCoroutine { coroutine ->
        loadAd(object : DTBAdCallback {
            override fun onFailure(error: AdError) {
                coroutine.resumeWithException(DTBException(error))
            }

            override fun onSuccess(response: DTBAdResponse) {
                coroutine.resume(response)
            }
        })
    }
}

/**
 * Loads a collection of Amazon requests in parallel and returns a list of successful responses.
 *
 * @param timeout the timeout in milliseconds each request must return by
 * @param onFailedRequest an optional action called on each request failure or timeout
 */
suspend fun Collection<DTBAdRequest>.loadAll(
    timeout: Long = 750,
    onFailedRequest: (DTBAdRequest, Throwable) -> Unit = { _, _ -> },
): List<DTBAdResponse> = coroutineScope {
    val inFlightRequests = map { dtbAdRequest ->
        async(Dispatchers.IO) {
            runCatching { dtbAdRequest.loadAd(ttl = timeout) }
                .onFailure { withContext(Dispatchers.Main) { onFailedRequest(dtbAdRequest, it) } }
                .getOrNull()
        }
    }
    inFlightRequests.awaitAll().filterNotNull()
}
