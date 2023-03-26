package com.adsbynimbus.android.sample.request

import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.addApsResponse
import com.amazon.device.ads.AdError
import com.amazon.device.ads.DTBAdCallback
import com.amazon.device.ads.DTBAdRequest
import com.amazon.device.ads.DTBAdResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
