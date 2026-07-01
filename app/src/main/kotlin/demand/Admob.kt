package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.ScreenAdLogger
import com.adsbynimbus.android.sample.rendering.disableAllExtensions
import com.adsbynimbus.extension.AdMobExtension
import kotlinx.coroutines.launch

val adMobPlacements = listOf(
    BuildConfig.ADMOB_BANNER,
    BuildConfig.ADMOB_NATIVE,
    BuildConfig.ADMOB_INTERSTITIAL,
    BuildConfig.ADMOB_REWARDED,
    BuildConfig.ADMOB_REWARDED_INTERSTITIAL,
)

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
            "Banner" -> lifecycleScope.launch {
                ads += Nimbus.bannerAd(item, AdSize.Banner) {
                    demand {
                        admobBanner(BuildConfig.ADMOB_BANNER)
                    }
                }.onEvent {
                    screenLogger.onAdEvent(it)
                }.onError {
                    screenLogger.onError(it)
                }.show(adFrame)
            }

            "MREC" -> lifecycleScope.launch {
                ads += Nimbus.bannerAd(item, AdSize.Mrec) {
                    demand {
                        admobBanner(BuildConfig.ADMOB_BANNER)
                    }
                }.onEvent {
                    screenLogger.onAdEvent(it)
                }.onError {
                    screenLogger.onError(it)
                }.show(adFrame)
            }

            "Interstitial" -> lifecycleScope.launch {
                ads += Nimbus.interstitialAd(item) {
                    demand {
                        admobInterstitial(BuildConfig.ADMOB_INTERSTITIAL)
                    }
                }.onEvent {
                    screenLogger.onAdEvent(it)
                }.onError {
                    screenLogger.onError(it)
                }.show(this@AdmobFragment)
            }

            "Rewarded" -> lifecycleScope.launch {
                ads += Nimbus.rewardedAd(item) {
                    demand {
                        admobRewarded(BuildConfig.ADMOB_REWARDED)
                    }
                }.onEvent {
                    screenLogger.onAdEvent(it)
                }.onError {
                    screenLogger.onError(it)
                }.show(this@AdmobFragment)
            }

            "Native" -> {
                // implementation for AdMob native differs slightly whether using admob or admob-nextgen,
                // see files on other build flavors for details on implementation
                AdMobNative.show(
                    adFrame = adFrame,
                    logs = logs,
                    item = item,
                    scope = lifecycleScope,
                )
            }
        }
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        ads.forEach { it.destroy() }
        AdMobNative.reset()
    }
}
