@file:OptIn(ExperimentalCoroutinesApi::class)

package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.view.*
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.Gravity.TOP
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.BuildConfig.*
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.ScreenAdLogger
import com.adsbynimbus.android.sample.rendering.disableAllExtensions
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.request.AdSize
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

class MobileFuseFragment : Fragment() {

    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        disableAllExtensions()
        Nimbus.extensions<MobileFuseExtension>()?.enabled = true

        when (val item = requireArguments().getString("item")) {
            "Banner" -> showBannerAd(MOBILE_FUSE_BANNER, AdSize.BANNER)
            "Banner With Refresh" -> showBannerAd(MOBILE_FUSE_BANNER, AdSize.BANNER, refreshable = true)
            "MREC" -> showBannerAd(MOBILE_FUSE_MREC, AdSize.MREC)
            "Interstitial" -> viewLifecycleOwner.lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                Nimbus.interstitialAd(MOBILE_FUSE_INTERSTITIAL) {
                    banner(size = AdSize.interstitial)
                }.onEvent {
                    logger.onAdEvent(it)
                }.onError {
                    logger.onError(it)
                }
            }
            "Rewarded" -> viewLifecycleOwner.lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                Nimbus.rewardedAd(MOBILE_FUSE_REWARDED).onEvent {
                    logger.onAdEvent(it)
                }.onError {
                    logger.onError(it)
                }
            }
        }
    }.root

    private fun LayoutInlineAdBinding.showBannerAd(item: String, size: AdSize, refreshable: Boolean = false) {
        viewLifecycleOwner.lifecycleScope.launch {
            val logger = ScreenAdLogger(identifier = item, logView = logs)
            ads += Nimbus.bannerAd(
                position = item,
                size = size,
                adPosition = Position.HEADER,
                refreshInterval = if (refreshable) 30 else 0,
            ).onEvent {
                logger.onAdEvent(it)
            }.onError {
                logger.onError(it)
            }.show(adFrame).also {
                it.adView?.updateLayoutParams<FrameLayout.LayoutParams> {
                    gravity = TOP or CENTER_HORIZONTAL
                    height = WRAP_CONTENT
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ads.forEach { it.destroy() }
    }
}
