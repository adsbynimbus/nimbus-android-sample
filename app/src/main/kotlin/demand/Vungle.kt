package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.databinding.VungleNativeAdBinding
import com.adsbynimbus.android.sample.rendering.*
import com.vungle.ads.NativeAd
import kotlinx.coroutines.launch

class VungleFragment : Fragment() {

    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        disableAllExtensions()
        Nimbus.extensions<VungleExtension>()?.enabled = true
        VungleExtension.nativeAdViewProvider = NativeRenderingNativeAdViewProvider()
        val item = requireArguments().getString("item") ?: ""
        val screenLogger = ScreenAdLogger(identifier = item, logView = logs)
        when (item) {
            "Vungle Banner" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.bannerAd(item, AdSize.Banner).onEvent {
                    screenLogger.onAdEvent(it)
                }.onError {
                    screenLogger.onError(it)
                }.show(adFrame)
            }

            "Vungle MREC" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.bannerAd(item, AdSize.Mrec).onEvent {
                    screenLogger.onAdEvent(it)
                }.onError {
                    screenLogger.onError(it)
                }.show(adFrame)
            }

            "Vungle Interstitial" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.interstitialAd(item).onEvent {
                    screenLogger.onAdEvent(it)
                }.onError {
                    screenLogger.onError(it)
                }.show(this@VungleFragment)
            }

            "Vungle Rewarded" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.rewardedAd(item).onEvent {
                    screenLogger.onAdEvent(it)
                }.onError {
                    screenLogger.onError(it)
                }.show(this@VungleFragment)
            }

            "Vungle Native" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.bannerAd(item, AdSize.Banner) {
                    native()
                }.onEvent {
                    screenLogger.onAdEvent(it)
                }.onError {
                    screenLogger.onError(it)
                }.show(adFrame)
            }
        }
        if (BuildConfig.VUNGLE_CONFIG_ID.isEmpty()) context?.showPropertyMissingDialog("sample_vungle_config_id")
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        ads.forEach { it.destroy() }
        VungleExtension.nativeAdViewProvider = null
    }
}

class NativeRenderingNativeAdViewProvider : VungleExtension.NativeAdViewProvider {
    override fun customViewForRendering(container: ViewGroup, nativeAd: NativeAd): View =
        VungleNativeAdBinding.inflate(LayoutInflater.from(container.context), container, false)
            .apply {
                nativeAdTitle.text = nativeAd.getAdTitle()
                nativeAdBody.text = nativeAd.getAdBodyText()
                nativeAd.getAdStarRating()?.let { rateTV.text = it.toString() }
                nativeAdCallToAction.visibility =
                    if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
                nativeAdCallToAction.text = nativeAd.getAdCallToActionText()
                nativeAdSponsoredLabel.text = nativeAd.getAdSponsoredText()

                nativeAd.registerViewForInteraction(
                    this.root, nativeAdMedia, nativeAdIcon, listOf(nativeAdTitle, nativeAdCallToAction),
                )
            }.root
}
