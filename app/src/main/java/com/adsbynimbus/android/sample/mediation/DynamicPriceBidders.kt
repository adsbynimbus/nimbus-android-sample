package com.adsbynimbus.android.sample.mediation

import android.content.Context
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.NimbusError
import com.adsbynimbus.lineitem.applyDynamicPrice
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.NimbusResponse
import com.adsbynimbus.request.RequestManager
import com.amazon.device.ads.AdError
import com.amazon.device.ads.DTBAdCallback
import com.amazon.device.ads.DTBAdRequest
import com.amazon.device.ads.DTBAdResponse
import com.amazon.device.ads.DTBAdUtil
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@JvmInline
value class NimbusBid(override val response: NimbusResponse) : Bid<NimbusResponse, AdManagerAdRequest.Builder> {
    override fun applyTargeting(target: AdManagerAdRequest.Builder) {
        response.applyDynamicPrice(target)
    }
}

class NimbusBidder(
    val manager: RequestManager = NimbusAdManager(),
    val requestProvider: () -> NimbusRequest,
) : Bidder<NimbusResponse, AdManagerAdRequest.Builder> {
    override suspend fun fetchBid(context: Context): NimbusBid? = suspendCancellableCoroutine { continuation ->
        manager.makeRequest(
            context, requestProvider(), object : RequestManager.Listener {
                override fun onAdResponse(nimbusResponse: NimbusResponse) {
                    continuation.resume(NimbusBid(nimbusResponse))
                }

                override fun onError(error: NimbusError) {
                    if (error.errorType == NimbusError.ErrorType.NO_BID) continuation.resume(null) else {
                        continuation.resumeWithException(error)
                    }
                }
            }
        )
    }
}

@JvmInline
value class ApsBid(override val response: DTBAdResponse) : Bid<DTBAdResponse, AdManagerAdRequest.Builder> {
    override fun applyTargeting(target: AdManagerAdRequest.Builder) {
        DTBAdUtil.INSTANCE.loadDTBParams(target, response)
    }
}

class ApsBidder(val adLoader: () -> DTBAdRequest) : Bidder<DTBAdResponse, AdManagerAdRequest.Builder> {
    override suspend fun fetchBid(context: Context): ApsBid? = suspendCancellableCoroutine { continuation ->
        adLoader().loadAd(object : DTBAdCallback {
            override fun onFailure(error: AdError) {
                if (error.code == AdError.ErrorCode.NO_FILL) continuation.resume(null) else {
                    continuation.resumeWithException(Throwable())
                }
            }

            override fun onSuccess(response: DTBAdResponse) {
                continuation.resume(ApsBid(response))
            }
        })
    }
}
