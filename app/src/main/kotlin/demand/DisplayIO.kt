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
import com.adsbynimbus.android.sample.databinding.LayoutDisplayIoNativeAdBinding
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.ScreenAdLogger
import com.adsbynimbus.android.sample.rendering.disableAllExtensions
import com.adsbynimbus.extension.DigitalTurbineExtension
import com.adsbynimbus.extension.DisplayIOExtension
import com.adsbynimbus.rtb.Position.Header
import com.brandio.ads.ads.supers.NativeAdInterface
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds


class DisplayIOFragment : Fragment() {

    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        disableAllExtensions()
        Nimbus.extensions<DisplayIOExtension>()?.enabled = true
        when (val item = requireArguments().getString("item")) {
            "Banner" -> lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.bannerAd(position = item, size = AdSize.Banner, adPosition = Header)
                    .onEvent {
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
            "MREC" -> lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.bannerAd(position = item, size = AdSize.Mrec, adPosition = Header)
                    .onEvent {
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
            "Interstitial" -> lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.interstitialAd(position = item) {
                    video()
                }.onEvent {
                    logger.onAdEvent(it)
                }.onError {
                    logger.onError(it)
                }.show(this@DisplayIOFragment, closeButtonDelay = 10.seconds)
            }
            "Rewarded" -> lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.rewardedAd(position = item).onEvent {
                    logger.onAdEvent(it)
                }.onError {
                    logger.onError(it)
                }.show(this@DisplayIOFragment, closeButtonDelay = 10.seconds)
            }
            "Native" -> lifecycleScope.launch {
                DisplayIOExtension.nativeAdViewProvider = DisplayIOExtension.NativeAdViewProvider { container, nativeAd ->
                    val binding = LayoutDisplayIoNativeAdBinding.inflate(LayoutInflater.from(requireContext()))
                    populateNativeAd(binding, nativeAd)
                    binding.root
                }

                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.inlineAd(item) {
                    native()
                }.onEvent {
                    logger.onAdEvent(it)
                }.onError {
                    logger.onError(it)
                }.show(adFrame)
            }
        }
    }.root

    private fun populateNativeAd(
        binding: LayoutDisplayIoNativeAdBinding,
        nativeAd: NativeAdInterface,
    ) {
        val adTitle: String? = nativeAd.headline
        val adDescription: String? = nativeAd.body
        val callToAction: String? = nativeAd.callToAction

        binding.adTitle.text = adTitle
        binding.adDescription.text = adDescription
        binding.adCtaBtn.text = callToAction

        nativeAd.registerViewForInteraction(binding.root,
            binding.mediaViewContainer, binding.adIcon, binding.adTitle, binding.adCtaBtn)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ads.forEach { it.destroy() }
        DigitalTurbineExtension.nativeAdViewProvider = null
    }
}
