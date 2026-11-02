package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.databinding.MetaNativeAdBinding
import com.adsbynimbus.android.sample.rendering.disableAllExtensions
import com.adsbynimbus.extension.MetaExtension
import com.adsbynimbus.extension.MetaExtension.NativeAdViewProvider
import com.facebook.ads.AdSettings
import com.facebook.ads.NativeAd
import kotlinx.coroutines.launch

/**
 * This Fragment shows what Meta ads look like when run through the Nimbus renderer but is not
 * indicative of normal usage as the Nimbus server determines which ad units to request based on the
 * request sent from the client.
 */
class MetaFragment : Fragment() {

    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {

        // Enabling Meta Ads test mode. Must not be set `true` in production.
        AdSettings.setTestMode(true)
        disableAllExtensions()
        Nimbus.extensions<MetaExtension>()?.enabled = true
        MetaExtension.nativeAdViewProvider = SampleMetaNativeAdViewProvider()
        when (val item = requireArguments().getString("item")) {
            "Meta Banner" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.bannerAd(item, AdSize.Banner).show(adFrame)
            }

            "Meta Native" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.inlineAd(item) {
                    native()
                }.show(adFrame)
            }

            "Meta Interstitial" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.interstitialAd(item).show(this@MetaFragment)
            }

            "Meta Rewarded Video" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.rewardedAd(item).show(this@MetaFragment)
            }
        }
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        ads.forEach { it.destroy() }
    }
}

class SampleMetaNativeAdViewProvider : NativeAdViewProvider {
    override fun viewForNativeAd(
        container: ViewGroup,
        nativeAd: NativeAd,
    ): View =
        MetaNativeAdBinding.inflate(LayoutInflater.from(container.context), container, false).apply {
            nativeAdTitle.text = nativeAd.advertiserName
            nativeAdBody.text = nativeAd.adBodyText
            nativeAdCallToAction.visibility = if (nativeAd.hasCallToAction()) View.VISIBLE else View.INVISIBLE
            nativeAdCallToAction.text = nativeAd.adCallToAction
            nativeAdSponsoredLabel.text = nativeAd.sponsoredTranslation
            val listClickables = listOf<View>(nativeAdTitle, nativeAdCallToAction)
            nativeAd.registerViewForInteraction(this.root, nativeAdMedia, nativeAdIcon, listClickables)
        }.root
}
