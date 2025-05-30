package com.adsbynimbus.android.sample.demand

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.Nimbus
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.*
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.request.*
import com.amazon.device.ads.*
import kotlinx.coroutines.*
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

    /* Set Nimbus as the Open Measurement Partner */
    AdRegistration.addCustomAttribute("omidPartnerName", Nimbus.sdkName)
    AdRegistration.addCustomAttribute("omidPartnerVersion", Nimbus.version)

    /* Optional: Enable APS logging / test mode to verify the integration */
    AdRegistration.enableLogging(true)
    AdRegistration.enableTesting(true)
}

class APSFragment : Fragment() {

    val adManager: NimbusAdManager = NimbusAdManager()
    private var adController: AdController? = null

    fun NimbusRequest.removeNonAPSDemand() = apply {
        interceptors.add(NimbusRequest.Interceptor {
            request.imp[0].ext.facebook_app_id = ""
            request.user?.ext = request.user?.ext?.apply {
                facebook_buyeruid = null
                unity_buyeruid = null
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
        val adapter = LogAdapter().apply { logs.useAsLogger(this) }
        when (val item = requireArguments().getString("item")) {
            "APS Banner With Refresh" -> lifecycleScope.launch {
                val nimbusRequest = NimbusRequest.forBannerAd(item, Format.BANNER_320_50)
                val apsRequest = DTBAdRequest(DTBAdNetworkInfo(DTBAdNetwork.NIMBUS)).apply {
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
                        adapter.appendLog("APS Request failed: ${it.message}")
                    }

                /* Show a Nimbus refreshing banner attached to the adFrame */
                adManager.showAd(
                    request = nimbusRequest.removeNonAPSDemand(),
                    refreshInterval = 30,
                    viewGroup = adFrame,
                    listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                        adController = controller.apply {
                            /* Replace the following with your own AdController.Listener implementation */
                            listeners.add(EmptyAdControllerListenerImplementation)
                        }
                    },
                )
            }
            "APS Interstitial Hybrid" -> lifecycleScope.launch {
                val nimbusRequest = NimbusRequest.forInterstitialAd(item)
                val apsInterstitial = DTBAdRequest(DTBAdNetworkInfo(DTBAdNetwork.NIMBUS)).apply {
                    setSizes(DTBAdSize.DTBInterstitialAdSize(BuildConfig.APS_STATIC))
                }
                val apsVideo = DTBAdRequest(DTBAdNetworkInfo(DTBAdNetwork.NIMBUS)).apply {
                    setSizes(DTBAdSize.DTBVideo(
                        resources.displayMetrics.widthPixels,
                        resources.displayMetrics.heightPixels, BuildConfig.APS_VIDEO))
                }

                /* See com.adsbynimbus.android.sample.request.Amazon.kt for implementation */
                listOf(apsInterstitial, apsVideo).loadAll { _, error ->
                    Timber.w(error, "APS Request failed: ${error.message }")
                }.forEach { apsResponse -> nimbusRequest.addApsResponse(apsResponse) }

                /* Show a Nimbus Interstitial ad with Display and Video in the same auction */
                adManager.showBlockingAd(
                    request = nimbusRequest.removeNonAPSDemand(),
                    activity = requireActivity(),
                    listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                        adController = controller.apply {
                            /* Replace the following with your own AdController.Listener implementation */
                            listeners.add(EmptyAdControllerListenerImplementation)
                        }
                    }
                )
            }
        }
    }.root

    override fun onDestroyView() {
        adController?.destroy()
        adController = null
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

/** Loads Amazon requests in parallel and adds the successful responses to the Nimbus request. */
suspend inline fun NimbusRequest.addAmazonAds(vararg requests: DTBAdRequest) = apply {
    requests.toList().loadAll().forEach { addApsResponse(it) }
}
