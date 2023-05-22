package com.adsbynimbus.android.sample.dynamicprice

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber

const val minRequestInterval = 30000L

fun interface Bid<R, T> {
    fun applyTargeting(target: T)

    val response: R? get() = null
}

fun interface Bidder<R, T> {
    suspend fun fetchBid(context: Context): Bid<R, T>?
}

class DynamicPriceManager(
    val bidders: Collection<Bidder<*, AdManagerAdRequest.Builder>>,
    val requestProvider: () -> AdManagerAdRequest.Builder = { AdManagerAdRequest.Builder() },
    val timeoutMillis: Long = 500L,
) {
    internal var lastRequestTime: Long = 0

    internal inline val timeSinceLastRequest: Long get() = System.currentTimeMillis() - lastRequestTime

    internal suspend fun Context.auction(
        request: AdManagerAdRequest.Builder = requestProvider(),
        timeBetweenRequests: Long = minRequestInterval,
        onBidReceived: (Bid<*, AdManagerAdRequest.Builder>) -> Unit = { },
    ) = bidders.asFlow().onStart {
        delay(timeBetweenRequests - timeSinceLastRequest)
        lastRequestTime = System.currentTimeMillis()
    }.flatMapMerge {
        flowOf(withTimeoutOrNull(timeoutMillis) { it.fetchBid(this@auction) }).flowOn(Dispatchers.IO).catch {
            Timber.d("Error making Nimbus Request", it)
        }
    }.filterNotNull().fold(request) { _, bid ->
        onBidReceived(bid)
        request.apply { bid.applyTargeting(this) }
    }

    fun loadAd(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        onBidReceivedListener: (Bid<*, AdManagerAdRequest.Builder>) -> Unit = { },
        onRequestReady: (AdManagerAdRequest.Builder) -> Unit,
    ) = lifecycleOwner.runCatching {
        lifecycleScope.launch { context.auction(onBidReceived = onBidReceivedListener).also { onRequestReady(it) } }
    }

    fun autoRefresh(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        refreshIntervalSeconds: Int = 30,
        onBidReceivedListener: (Bid<*, AdManagerAdRequest.Builder>) -> Unit = { },
        onRequestReady: (AdManagerAdRequest.Builder) -> Unit,
    ) = lifecycleOwner.runCatching {
        lifecycleScope.launch {
            val refreshIntervalMillis: Long = refreshIntervalSeconds.coerceAtLeast(30) * 1000L
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) context.auction(
                    timeBetweenRequests = refreshIntervalMillis,
                    onBidReceived = onBidReceivedListener,
                ).also { onRequestReady(it) }
            }
        }
    }
}

fun Fragment.dynamicPriceAuction(
    bidders: Collection<Bidder<*, AdManagerAdRequest.Builder>>,
    refreshEvery: Int = 0,
    onBidReceived: (Bid<*, AdManagerAdRequest.Builder>) -> Unit = { },
    requestProvider: () -> AdManagerAdRequest.Builder = { AdManagerAdRequest.Builder() },
    timeoutMillis: Long = 500L,
    onRequestReady: (AdManagerAdRequest.Builder) -> Unit,
) = DynamicPriceManager(bidders, requestProvider, timeoutMillis).also {
    if (refreshEvery > 0) it.autoRefresh(
        context = requireContext(),
        lifecycleOwner = this,
        onBidReceivedListener = onBidReceived,
        onRequestReady = onRequestReady,
    ) else it.loadAd(
        context = requireContext(),
        lifecycleOwner = this,
        onBidReceivedListener = onBidReceived,
        onRequestReady = onRequestReady,)
}
