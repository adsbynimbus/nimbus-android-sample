package com.adsbynimbus.android.sample.demand

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.Gravity.TOP
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.databinding.LayoutDigitalTurbineNativeAdBinding
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.ScreenAdLogger
import com.adsbynimbus.android.sample.rendering.disableAllExtensions
import com.adsbynimbus.extension.DigitalTurbineExtension
import com.adsbynimbus.rtb.Position.Header
import com.fyber.inneractive.sdk.external.MediaView
import com.fyber.inneractive.sdk.external.NativeAdContent
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds


class DigitalTurbineFragment : Fragment() {

    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        disableAllExtensions()
        Nimbus.extensions<DigitalTurbineExtension>()?.enabled = true
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
                }.show(this@DigitalTurbineFragment, closeButtonDelay = 10.seconds)
            }
            "Rewarded" -> lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.rewardedAd(position = item).onEvent {
                    logger.onAdEvent(it)
                }.onError {
                    logger.onError(it)
                }.show(this@DigitalTurbineFragment, closeButtonDelay = 10.seconds)
            }
            "Native" -> lifecycleScope.launch {

                DigitalTurbineExtension.nativeAdViewProvider = DigitalTurbineExtension.NativeAdViewProvider { container, nativeAd ->
                    val binding = LayoutDigitalTurbineNativeAdBinding.inflate(LayoutInflater.from(requireContext()))
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
        binding: LayoutDigitalTurbineNativeAdBinding,
        nativeAd: NativeAdContent,
    ) {
        val adTitle: String? = nativeAd.adTitle
        val adDescription: String? = nativeAd.adDescription
        val appIconUri: Uri? = nativeAd.appIcon
        val callToAction: String? = nativeAd.adCallToAction
        val rating: Float? = nativeAd.rating
        val mediaView: MediaView? = nativeAd.mediaView
        val mediaAspectRatio: Float? = nativeAd.mediaAspectRatio

        binding.adTitle.text = adTitle
        binding.adDescription.text = adDescription
        binding.adIcon.setImageURI(appIconUri)
        binding.adCtaBtn.text = callToAction
        rating?.let { binding.adRating.rating }
        binding.mediaViewContainer.addView(mediaView)
        binding.mediaViewContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
            dimensionRatio = mediaAspectRatio.toString()
        }

        nativeAd.registerViewsForInteraction(binding.root,
            nativeAd.mediaView, binding.adIcon, listOf(binding.adCtaBtn, binding.adRating, binding.adDescription, binding.adTitle))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ads.forEach { it.destroy() }
        DigitalTurbineExtension.nativeAdViewProvider = null
    }
}
