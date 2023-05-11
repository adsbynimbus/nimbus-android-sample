package com.adsbynimbus.android.sample.demand

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.Nimbus
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.AdEvent
import com.adsbynimbus.request.*
import com.amazon.device.ads.AdError
import com.amazon.device.ads.AdRegistration
import com.amazon.device.ads.DTBAdCallback
import com.amazon.device.ads.DTBAdNetwork
import com.amazon.device.ads.DTBAdNetworkInfo
import com.amazon.device.ads.DTBAdRequest
import com.amazon.device.ads.DTBAdResponse
import com.amazon.device.ads.DTBAdSize
import com.amazon.device.ads.MRAIDPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Demonstrates how to initialize the APS SDK for Nimbus */
fun Context.initializeAmazonPublisherServices(appKey: String) {
    /* Initialize APS SDK */
    AdRegistration.getInstance(appKey, this) // this is the application context

    /* Set the MRAID Policy */
    AdRegistration.setMRAIDSupportedVersions(arrayOf("1.0", "2.0", "3.0"))
    AdRegistration.setMRAIDPolicy(MRAIDPolicy.CUSTOM)

    /* Set Nimbus as the Mediator */
    AdRegistration.setAdNetworkInfo(DTBAdNetworkInfo(DTBAdNetwork.NIMBUS))

    /* Set Nimbus as the Open Measurement Partner */
    AdRegistration.addCustomAttribute("omidPartnerName", Nimbus.sdkName)
    AdRegistration.addCustomAttribute("omidPartnerVersion", Nimbus.version)

    /* Optional: Enable APS logging / test mode to verify the integration */
    AdRegistration.enableLogging(true)
    AdRegistration.enableTesting(true)
}

class APSFragment : Fragment(), NimbusRequest.Interceptor {

    val adManager: NimbusAdManager = NimbusAdManager()
    private var adController: AdController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        RequestManager.interceptors.add(this@APSFragment)
        when (requireArguments().getString("item")) {
            "APS Banner" -> lifecycleScope.launch {
                val nimbusRequest = NimbusRequest.forBannerAd("test_banner", Format.BANNER_320_50)
                val apsRequest = DTBAdRequest().apply {
                    setSizes(DTBAdSize(320, 50, BuildConfig.APS_BANNER))
                }

                /* See com.adsbynimbus.android.sample.request.Amazon.kt for implementation */
                runCatching { apsRequest.loadAd() }
                    .onSuccess { apsResponse ->
                        nimbusRequest.addApsResponse(apsResponse)
                        /* For refreshing banner requests */
                        nimbusRequest.addApsLoader(apsResponse.adLoader)
                    }.onFailure {
                        /* Add the loader from the AdError for refreshing banners */
                        if (it is DTBException) nimbusRequest.addApsLoader(it.error.adLoader)
                    }

                /* Show a Nimbus refreshing banner attached to the adFrame */
                adManager.showAd(nimbusRequest, refreshInterval = 30, viewGroup = adFrame) {
                    adController = it.apply { addListener("Banner Controller") }
                }
            }
            "APS Interstitial Hybrid" -> lifecycleScope.launch {
                val nimbusRequest = NimbusRequest.forInterstitialAd("test_interstitial_with_aps")
                val apsInterstitial = DTBAdRequest().apply {
                    setSizes(DTBAdSize.DTBInterstitialAdSize(BuildConfig.APS_STATIC))
                }
                val apsVideo = DTBAdRequest().apply {
                    setSizes(DTBAdSize.DTBVideo(
                        resources.displayMetrics.widthPixels,
                        resources.displayMetrics.heightPixels, BuildConfig.APS_VIDEO))
                }

                /* See com.adsbynimbus.android.sample.request.Amazon.kt for implementation */
                listOf(apsInterstitial, apsVideo).loadAll().forEach { apsResponse ->
                    nimbusRequest.addApsResponse(apsResponse)
                }

                /* Show a Nimbus Interstitial ad with Display and Video in the same auction */
                adManager.showBlockingAd(nimbusRequest, requireActivity()) {
                    adController = it.apply { addListener("Interstitial Hybrid Controller") }
                }
            }
        }
    }.root

    override fun onDestroyView() {
        adController?.destroy()
        adController = null
        RequestManager.interceptors.remove(this)
        super.onDestroyView()
    }

    override fun modifyRequest(request: NimbusRequest) {
        request.request.imp[0].ext.facebook_app_id = ""
        request.request.user?.ext = request.request.user?.ext?.apply {
            facebook_buyeruid = null
            unity_buyeruid = null
            vungle_buyeruid = null
        }
    }

    private fun AdController.addListener(controllerName: String) {
        listeners().add(object : AdController.Listener {
            override fun onAdEvent(adEvent: AdEvent) {
                Timber.i("$controllerName: %s", adEvent.name)
                if (adEvent == AdEvent.DESTROYED) adController = null
            }

            override fun onError(error: NimbusError) {
                Timber.e("$controllerName: %s", error.message)
                adController = null
            }
        })
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
suspend fun DTBAdRequest.loadAd(ttl: Long = 750): DTBAdResponse = withTimeout(ttl) {
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
    val outerScope = coroutineContext
    val inFlightRequests = map { dtbAdRequest ->
        async(Dispatchers.IO) {
            runCatching { dtbAdRequest.loadAd(ttl = timeout) }
                .onFailure { withContext(outerScope) { onFailedRequest(dtbAdRequest, it) } }
                .getOrNull()
        }
    }
    inFlightRequests.awaitAll().filterNotNull()
}

/** Loads Amazon requests in parallel and adds the successful responses to the Nimbus request. */
suspend inline fun NimbusRequest.addAmazonAds(vararg requests: DTBAdRequest) = apply {
    requests.toList().loadAll().forEach { addApsResponse(it) }
}
