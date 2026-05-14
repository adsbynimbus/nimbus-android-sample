package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.disableAllExtensions
import com.adsbynimbus.extension.MetaExtension
import com.facebook.ads.AdSettings
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

        when (val item = requireArguments().getString("item")) {
            "Meta Banner" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.bannerAd(item, AdSize.Banner).onEvent {
                    Log.i("Test3.0", "Event $it")
                }.show(adFrame)
            }

            "Meta Native" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.inlineAd(item) {
                    native()
                }.onEvent {
                    Log.i("Test3.0", "Event $it")
                }.show(adFrame)
            }

            "Meta Interstitial" -> viewLifecycleOwner.lifecycleScope.launch {
                Nimbus.interstitialAd(item).onEvent {
                    Log.i("Test3.0", "Event $it")
                }.show(this@MetaFragment)
            }

            "Meta Rewarded Video" -> viewLifecycleOwner.lifecycleScope.launch {
                 Nimbus.rewardedAd(item).onEvent {
                    Log.i("Test3.0", "Event $it")
                }.show(this@MetaFragment)
            }
        }
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        ads.forEach { it.destroy() }
    }
}
