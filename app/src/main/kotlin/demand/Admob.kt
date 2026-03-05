package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.GoogleNativeAdBinding
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.ScreenAdLogger
import com.adsbynimbus.android.sample.rendering.disableAllExtensions
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.request.*
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.launch

class AdmobFragment : Fragment() {

    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        disableAllExtensions()
        Nimbus.extensions<AdMobExtension>()?.enabled = true
        val item = requireArguments().getString("item") ?: ""
        val screenLogger = ScreenAdLogger(identifier = item, logView = logs)
        when (item) {
            "Banner" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.bannerAd(item, Format.BANNER_320_50) {
                    demand {
                        admobBanner(BuildConfig.ADMOB_BANNER)
                    }
                }.onEvent {
                    screenLogger.onAdEvent(it)
                }.onError {
                    screenLogger.onError(it)
                }.show(adFrame)
            }
            "MREC" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.bannerAd(item, Format.MREC) {
                    demand {
                        admobBanner(BuildConfig.ADMOB_BANNER)
                    }
                }.onEvent {
                    screenLogger.onAdEvent(it)
                }.onError {
                    screenLogger.onError(it)
                }.show(adFrame)
            }
            "Interstitial" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.interstitialAd(item) {
                    demand {
                        adMobInterstitial(BuildConfig.ADMOB_INTERSTITIAL)
                    }
                }.onEvent {
                    screenLogger.onAdEvent(it)
                }.onError {
                    screenLogger.onError(it)
                }.show(this@AdmobFragment)
            }
            "Rewarded" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.rewardedAd(item) {
                    demand {
                        adMobRewarded(BuildConfig.ADMOB_REWARDED)
                    }
                }.onEvent {
                    screenLogger.onAdEvent(it)
                }.onError {
                    screenLogger.onError(it)
                }.show(this@AdmobFragment)
            }
            "Native" -> {
                Nimbus.extensions<AdMobExtension>()?.delegate = AdMobExtension.Delegate { container, nativeAd ->
                    GoogleNativeAdBinding.inflate(LayoutInflater.from(container.context)).apply {
                        populateNativeAdView(nativeAd, this)
                    }.root
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    ads += Nimbus.nativeAd(item) {
                        demand {
                            adMobNative(BuildConfig.ADMOB_NATIVE)
                        }
                    }.onEvent {
                        screenLogger.onAdEvent(it)
                    }.onError {
                        screenLogger.onError(it)
                    }.show(adFrame)
                }
            }
        }
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        ads.forEach { it.destroy() }
        Nimbus.extensions<AdMobExtension>()?.delegate = null
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
