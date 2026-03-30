package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.databinding.LayoutMintegralNativeAdBinding
import com.adsbynimbus.android.sample.rendering.ScreenAdLogger
import com.adsbynimbus.android.sample.rendering.disableAllExtensions
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.request.AdSize
import com.mbridge.msdk.out.Campaign
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MintegralFragment  : Fragment() {

    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        disableAllExtensions()
        Nimbus.extensions<MintegralExtension>()?.enabled = true

        when (val item = requireArguments().getString("item")) {
            "Banner"  -> viewLifecycleOwner.lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.bannerAd(position = item, size = AdSize.Banner, adPosition = Position.HEADER)
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
            "MREC" -> viewLifecycleOwner.lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.bannerAd(position = item, size = AdSize.MREC, adPosition = Position.HEADER)
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
            "Interstitial" -> viewLifecycleOwner.lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.interstitialAd(position = item) {
                    video()
                }.onEvent {
                    logger.onAdEvent(it)
                }.onError {
                    logger.onError(it)
                }.show(this@MintegralFragment, closeButtonDelay = 10.seconds)
            }
            "Rewarded" -> viewLifecycleOwner.lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.rewardedAd(position = item).onEvent {
                    logger.onAdEvent(it)
                }.onError {
                    logger.onError(it)
                }.show(this@MintegralFragment, closeButtonDelay = 10.seconds)
            }
            "Native" -> {
                MintegralExtension.nativeAdViewProvider = MintegralExtension.NativeAdViewProvider { container, campaign ->
                    LayoutMintegralNativeAdBinding.inflate(LayoutInflater.from(container.context)).apply {
                        populateNativeAdView(campaign)
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
                            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                            height = WRAP_CONTENT
                        }
                    }
                }
            }
        }
    }.root

    private fun LayoutMintegralNativeAdBinding.populateNativeAdView(campaign: Campaign) {
        // resize ad choice accordingly
        customChoice.updateLayoutParams {
            width = campaign.adchoiceSizeWidth
            height = campaign.adchoiceSizeHeight
        }

        // initialize the campaign
        customChoice.setCampaign(campaign)
        customMedia.setNativeAd(campaign)

        // update other custom fields using info from campaign
        customTitle.text = campaign.appName
        customDesc.text = campaign.appDesc
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ads.forEach { it.destroy() }
        MintegralExtension.nativeAdViewProvider = null
    }
}
