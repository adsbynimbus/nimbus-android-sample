package com.adsbynimbus.android.sample.demand

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.AdmobNextGenNativeAdBinding
import com.adsbynimbus.android.sample.rendering.ScreenAdLogger
import com.adsbynimbus.extension.AdMobExtension
import com.adsbynimbus.request.AdMobNativeAdOptions
import com.google.android.libraries.ads.mobile.sdk.common.AdChoicesPlacement
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * These only differ by the type of AdMob NativeAd class.
 */
object AdMobNative {
    var ads = mutableListOf<Ad>()

    fun show(adFrame: ViewGroup, logs: RecyclerView, item: String, scope: CoroutineScope) {
        AdMobExtension.nativeAdViewProvider = AdMobExtension.NativeAdViewProvider { container, nativeAd ->
            AdmobNextGenNativeAdBinding.inflate(LayoutInflater.from(container.context)).apply {
                populateNativeAdView(nativeAd, this)
            }.root
        }

        scope.launch {
            val screenLogger = ScreenAdLogger(identifier = item, logView = logs)
            ads += Nimbus.inlineAd(item) {
                native()
                demand {
                    admobNative(
                        adUnitId = BuildConfig.ADMOB_NATIVE,
                        nativeAdOptions = AdMobNativeAdOptions(
                            disableImageLoading = false,
                            mediaAspectRatio = NativeAd.NativeMediaAspectRatio.SQUARE,
                            preferredAdChoicesPosition = AdChoicesPlacement.TOP_RIGHT,
                        )
                    )
                }
            }.onEvent {
                screenLogger.onAdEvent(it)
            }.onError {
                screenLogger.onError(it)
            }.show(adFrame)
        }
    }

    fun reset() {
        AdMobExtension.nativeAdViewProvider = null
        ads.forEach { it.destroy() }
        ads.clear()
    }

    private fun populateNativeAdView(nativeAd: NativeAd, binding: AdmobNextGenNativeAdBinding) {
        val nativeAdView = binding.root

        // Set other ad assets.
        nativeAdView.headlineView = binding.adHeadline
        nativeAdView.bodyView = binding.adBody
        nativeAdView.callToActionView = binding.adCallToAction
        nativeAdView.iconView = binding.adAppIcon
        nativeAdView.priceView = binding.adPrice
        nativeAdView.starRatingView = binding.adStars
        nativeAdView.storeView = binding.adStore
        nativeAdView.advertiserView = binding.adAdvertiser

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        binding.adHeadline.text = nativeAd.headline
        binding.adMedia.mediaContent = nativeAd.mediaContent

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            binding.adBody.visibility = View.INVISIBLE
        } else {
            binding.adBody.visibility = View.VISIBLE
            binding.adBody.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            binding.adCallToAction.visibility = View.INVISIBLE
        } else {
            binding.adCallToAction.visibility = View.VISIBLE
            binding.adCallToAction.text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            binding.adAppIcon.visibility = View.GONE
        } else {
            binding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
            binding.adAppIcon.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            binding.adPrice.visibility = View.INVISIBLE
        } else {
            binding.adPrice.visibility = View.VISIBLE
            binding.adPrice.text = nativeAd.price
        }

        if (nativeAd.store == null) {
            binding.adStore.visibility = View.INVISIBLE
        } else {
            binding.adStore.visibility = View.VISIBLE
            binding.adStore.text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            binding.adStars.visibility = View.INVISIBLE
        } else {
            binding.adStars.rating = nativeAd.starRating!!.toFloat()
            binding.adStars.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            binding.adAdvertiser.visibility = View.INVISIBLE
        } else {
            binding.adAdvertiser.text = nativeAd.advertiser
            binding.adAdvertiser.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        nativeAdView.registerNativeAd(nativeAd, binding.adMedia)
    }
}
