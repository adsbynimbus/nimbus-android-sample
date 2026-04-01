package com.adsbynimbus.android.sample.demand

import android.content.Context
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
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.databinding.MolocoNativeAdBinding
import com.adsbynimbus.android.sample.rendering.ScreenAdLogger
import com.adsbynimbus.android.sample.rendering.disableAllExtensions
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.render.NimbusMolocoNativeAd
import com.adsbynimbus.request.AdSize
import com.moloco.sdk.internal.MolocoLogger
import com.moloco.sdk.publisher.Moloco
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

fun initializeMoloco(context: Context, appKey: String) {
    if (!Moloco.isInitialized) {
        MolocoLogger.logEnabled = true
        MolocoExtension.initialize(context, appKey = appKey)
    }
}

class MolocoFragment : Fragment() {

    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        initializeMoloco(requireContext(), BuildConfig.MOLOCO_APP_KEY)
        disableAllExtensions()
        Nimbus.extensions<MolocoExtension>()?.enabled = true
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
                            gravity = TOP or CENTER_HORIZONTAL
                            height = WRAP_CONTENT
                        }
                    }
            }
            "MREC" -> viewLifecycleOwner.lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.bannerAd(position = item, size = AdSize.MREC, adPosition = Position.Header)
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
            "Interstitial" -> viewLifecycleOwner.lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.interstitialAd(position = item) {
                    video()
                }.onEvent {
                    logger.onAdEvent(it)
                }.onError {
                    logger.onError(it)
                }.show(this@MolocoFragment, closeButtonDelay = 10.seconds)
            }
            "Rewarded" -> viewLifecycleOwner.lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.rewardedAd(position = item).onEvent {
                    logger.onAdEvent(it)
                }.onError {
                    logger.onError(it)
                }.show(this@MolocoFragment, closeButtonDelay = 10.seconds)
            }
            "Native" -> {
                MolocoExtension.nativeAdViewProvider = MolocoExtension.NativeAdViewProvider { container, nimbusMolocoNativeAd ->
                    MolocoNativeAdBinding.inflate(LayoutInflater.from(container.context)).apply {
                        populateNativeAdView(nimbusMolocoNativeAd, this)
                    }.root
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    val logger = ScreenAdLogger(identifier = item, logView = logs)
                    ads += Nimbus.bannerAd(position = item, size = AdSize.MREC) {
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
        }
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        ads.forEach { it.destroy() }
        MolocoExtension.nativeAdViewProvider = null
    }

    private fun populateNativeAdView(nativeAd: NimbusMolocoNativeAd, binding: MolocoNativeAdBinding) = with(nativeAd) {
        assets.title?.let {
            binding.adHeadline.text = it
            binding.adHeadline.visibility = View.VISIBLE
        } ?: run { binding.adHeadline.visibility = View.INVISIBLE }

        assets.description?.let {
            binding.adBody.text = it
            binding.adBody.visibility = View.VISIBLE
        } ?: run { binding.adBody.visibility = View.INVISIBLE }

        assets.callToActionText?.let {
            binding.adCallToAction.text = it
            binding.adCallToAction.visibility = View.VISIBLE
        } ?: run { binding.adCallToAction.visibility = View.INVISIBLE }

        assets.sponsorText?.let {
            binding.adAdvertiser.text = it
            binding.adAdvertiser.visibility = View.VISIBLE
        } ?: run { binding.adAdvertiser.visibility = View.INVISIBLE }

        assets.rating?.let {
            binding.adStars.rating = it
            binding.adStars.visibility = View.VISIBLE
        } ?: run { binding.adStars.visibility = View.INVISIBLE }

        assets.mediaView?.let {
            binding.mediaContainer.addView(it)
        }

        assets.iconUri?.let {
            binding.adAppIcon.setImageURI(it)
            binding.adAppIcon.visibility = View.VISIBLE
        } ?: run { binding.adAppIcon.visibility = View.INVISIBLE }

        clickableViews.add(binding.adCallToAction)
        clickableViews.add(binding.adHeadline)
    }
}

