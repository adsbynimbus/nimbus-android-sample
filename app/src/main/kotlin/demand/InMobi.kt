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
import com.adsbynimbus.android.sample.BuildConfig.INMOBI_ACCOUNT_ID
import com.adsbynimbus.android.sample.databinding.InmobiNativeAdBinding
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.ScreenAdLogger
import com.adsbynimbus.android.sample.rendering.disableAllExtensions
import com.adsbynimbus.openrtb.enumerations.Position.Header
import com.adsbynimbus.request.AdSize
import com.inmobi.ads.InMobiNative
import com.inmobi.sdk.InMobiSdk
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds


fun initializeInMobi(accountId: String) {
    InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG)
    if (InMobiSdk.isSDKInitialized()) return
    InMobiExtension.initialize(accountId = accountId)
}

class InMobiFragment : Fragment() {

    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        initializeInMobi(INMOBI_ACCOUNT_ID)
        disableAllExtensions()
        Nimbus.extensions<InMobiExtension>()?.enabled = true
        when (val item = requireArguments().getString("item")) {
            "Banner" -> viewLifecycleOwner.lifecycleScope.launch {
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
            "Native" -> {
                InMobiExtension.nativeAdViewProvider = InMobiExtension.NativeAdViewProvider { container, nativeAd ->
                    InmobiNativeAdBinding.inflate(LayoutInflater.from(container.context)).apply {
                        populateNativeAdView(nativeAd, this)
                    }.root
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    val logger = ScreenAdLogger(identifier = item, logView = logs)
                    ads += Nimbus.bannerAd(position = item, size = AdSize.Banner) {
                        native()
                    }.onEvent {
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
            "Interstitial" -> viewLifecycleOwner.lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.interstitialAd(position = item) {
                    video()
                }.onEvent {
                    logger.onAdEvent(it)
                }.onError {
                    logger.onError(it)
                }.show(this@InMobiFragment, closeButtonDelay = 10.seconds)
            }
            "Rewarded" -> viewLifecycleOwner.lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.rewardedAd(position = item).onEvent {
                    logger.onAdEvent(it)
                }.onError {
                    logger.onError(it)
                }.show(this@InMobiFragment, closeButtonDelay = 10.seconds)
            }
        }
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        InMobiExtension.nativeAdViewProvider = null
        ads.forEach { it.destroy() }
    }

    private fun populateNativeAdView(nativeAd: InMobiNative, binding: InmobiNativeAdBinding) {
        nativeAd.adTitle?.let {
            binding.adHeadline.text = it
            binding.adHeadline.visibility = View.VISIBLE
        } ?: run { binding.adHeadline.visibility = View.INVISIBLE }

        nativeAd.adDescription?.let {
            binding.adBody.text = it
            binding.adBody.visibility = View.VISIBLE
        } ?: run { binding.adBody.visibility = View.INVISIBLE }

        nativeAd.adCtaText?.let {
            binding.adCallToAction.text = it
            binding.adCallToAction.visibility = View.VISIBLE
        } ?: run { binding.adCallToAction.visibility = View.INVISIBLE }

        nativeAd.adRating.takeIf { it > 0 }?.let {
            binding.adStars.rating = it
            binding.adStars.visibility = View.VISIBLE
        } ?: run { binding.adStars.visibility = View.INVISIBLE }

        val view = nativeAd.getPrimaryViewOfWidth(requireContext(), null, binding.mediaContainer, binding.root.width)
        binding.mediaContainer.addView(view)
    }
}
