package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.ScreenAdLogger
import com.adsbynimbus.android.sample.rendering.showPropertyMissingDialog
import com.adsbynimbus.request.AdSize
import com.vungle.ads.NativeAd
import com.vungle.ads.internal.ui.view.MediaView
import kotlinx.coroutines.launch

class VungleFragment : Fragment() {

    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        Nimbus.extensions<VungleExtension>()?.enabled = true
        VungleExtension.nativeAdViewProvider = NativeRenderingNativeAdViewProvider()
        val item = requireArguments().getString("item") ?: ""
        val screenLogger = ScreenAdLogger(identifier = item, logView = logs)
        when (item) {
            "Vungle Banner" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.bannerAd(item, AdSize.BANNER).onEvent {
                    screenLogger.onAdEvent(it)
                }.onError {
                    screenLogger.onError(it)
                }.show(adFrame)
            }

            "Vungle MREC" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.bannerAd(item, AdSize.MREC).onEvent {
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
                ads += Nimbus.bannerAd(item, AdSize.BANNER) {
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
        LayoutInflater.from(container.context)
            .inflate(R.layout.vungle_native_ad, container, false).apply {
                val nativeAdIcon = findViewById<ImageView>(R.id.native_ad_icon)
                val nativeAdTitle = findViewById<TextView>(R.id.native_ad_title)
                val nativeAdMedia = findViewById<MediaView>(R.id.native_ad_media)
                val rateView = findViewById<TextView>(R.id.rateTV)
                val nativeAdBody = findViewById<TextView>(R.id.native_ad_body)
                val sponsoredLabel = findViewById<TextView>(R.id.native_ad_sponsored_label)
                val nativeAdCallToAction = findViewById<Button>(R.id.native_ad_call_to_action)

                nativeAdTitle.text = nativeAd.getAdTitle()
                nativeAdBody.text = nativeAd.getAdBodyText()
                nativeAd.getAdStarRating()?.let { rateView.text = it.toString() }
                nativeAdCallToAction.visibility =
                    if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
                nativeAdCallToAction.text = nativeAd.getAdCallToActionText()
                sponsoredLabel.text = nativeAd.getAdSponsoredText()

                nativeAd.registerViewForInteraction(
                    this as FrameLayout, nativeAdMedia, nativeAdIcon, listOf(nativeAdTitle, nativeAdCallToAction),
                )
            }
}
