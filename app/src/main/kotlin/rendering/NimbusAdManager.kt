package com.adsbynimbus.android.sample.rendering

import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.databinding.LayoutAdsInListBinding
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.request.AdSize
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class AdManagerFragment : Fragment() {

    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        disableAllExtensions()
        when (val item = requireArguments().getString("item")) {
            "Banner" -> viewLifecycleOwner.lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.bannerAd(position = item, size = AdSize.Banner, adPosition = Position.Header)
                    .onEvent {
                        logger.onAdEvent(it)
                    }.onError {
                        logger.onError(it)
                    }.show(adFrame).also {
                        it.adView?.updateLayoutParams<FrameLayout.LayoutParams> {
                            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                            height = WRAP_CONTENT
                        }
                    }
            }

            "Banner With Refresh" -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val logger = ScreenAdLogger(identifier = item, logView = logs)
                    ads += Nimbus.bannerAd(
                        position = item,
                        size = AdSize.Banner,
                        adPosition = Position.Header,
                        refreshInterval = 30,
                    ).onEvent {
                        logger.onAdEvent(it)
                    }.onError {
                        logger.onError(it)
                    }.show(adFrame).also {
                        it.adView?.updateLayoutParams<FrameLayout.LayoutParams> {
                            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                            height = WRAP_CONTENT
                        }
                    }
                }
            }

            "Video With Refresh" -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val logger = ScreenAdLogger(identifier = item, logView = logs)
                    ads += Nimbus.inlineAd(position = item, refreshInterval = 30.seconds) {
                        video()
                    }.onEvent {
                        logger.onAdEvent(it)
                    }.onError {
                        logger.onError(it)
                    }.show(adFrame).also {
                        it.adView?.updateLayoutParams<FrameLayout.LayoutParams> {
                            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                            height = WRAP_CONTENT
                        }
                    }
                }
            }

            "Inline Video" -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val logger = ScreenAdLogger(identifier = item, logView = logs)
                    ads += Nimbus.inlineAd(position = item) {
                        video()
                    }.onEvent {
                        logger.onAdEvent(it)
                    }.onError {
                        logger.onError(it)
                    }.show(adFrame).also {
                        it.adView?.updateLayoutParams<FrameLayout.LayoutParams> {
                            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                            height = WRAP_CONTENT
                        }
                    }
                }
            }

            "Interstitial Hybrid" -> lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.interstitialAd(position = item) {
                    video()
                }.onEvent {
                    logger.onAdEvent(it)
                }.onError {
                    logger.onError(it)
                }.show(this@AdManagerFragment, closeButtonDelay = 10.seconds)
            }
            "Interstitial Static" -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val logger = ScreenAdLogger(identifier = item, logView = logs)
                    ads += Nimbus.fullScreenAd(position = item) {
                        banner(size = AdSize.InterstitialPortrait)
                    }.onEvent {
                        logger.onAdEvent(it)
                    }.onError {
                        logger.onError(it)
                    }.show(this@AdManagerFragment)
                }
            }

            "Interstitial Video" -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val logger = ScreenAdLogger(identifier = item, logView = logs)
                    ads += Nimbus.fullScreenAd(position = item) {
                        video()
                    }.onEvent {
                        logger.onAdEvent(it)
                    }.onError {
                        logger.onError(it)
                    }.show(this@AdManagerFragment)
                }
            }

            "Rewarded Video" -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val logger = ScreenAdLogger(identifier = item, logView = logs)
                    ads += Nimbus.rewardedAd(position = item).onEvent {
                        logger.onAdEvent(it)
                    }.onError {
                        logger.onError(it)
                    }.show(this@AdManagerFragment)
                }
            }

            "Ads in ScrollView" -> {
                LayoutAdsInListBinding.inflate(inflater, adFrame, true).apply {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val logger = ScreenAdLogger(identifier = item, logView = logs)

                        ads += Nimbus.bannerAd(
                            position = "$item Banner",
                            size = AdSize.Banner,
                            refreshInterval = 30,
                            adPosition = Position.Header,
                        ).onEvent {
                            logger.onAdEvent(it)
                        }.onError {
                            logger.onError(it)
                        }.show(adFrameBanner)

                        ads += Nimbus.inlineAd(position = "$item Banner", refreshInterval = 30.seconds) {
                            banner(size = AdSize.interstitial)
                        }.onEvent {
                            logger.onAdEvent(it)
                        }.onError {
                            logger.onError(it)
                        }.show(adFrameImage)

                        ads += Nimbus.inlineAd(position = "$item Banner") {
                            video()
                        }.onEvent {
                            logger.onAdEvent(it)
                        }.onError {
                            logger.onError(it)
                        }.show(adFrameVideo)
                    }
                }
            }
        }
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        ads.forEach { it.destroy() }
    }
}

/** This is necessary in the sample app to prevent samples returning ads from other demand networks,
 * production apps should not need to implement something similar */
fun disableAllExtensions() {
    Nimbus.extensions.forEach { it.enabled = false }
}
