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
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.RequestManager
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class AdManagerFragment : Fragment(), NimbusRequest.Interceptor {

    val adManager: NimbusAdManager = NimbusAdManager()
    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        RequestManager.interceptors.add(this@AdManagerFragment)
        when (val item = requireArguments().getString("item")) {
            "Banner" -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val logger = ScreenAdLogger(identifier = item, logView = logs)
                    ads += Nimbus.bannerAd(position = item, size = Format.BANNER_320_50, adPosition = Position.HEADER)
                        .onEvent {
                            logger.onAdEvent(it)
                        }.onError {
                            logger.onError(it)
                        }.load(adFrame).show(adFrame).also {
                            it.adView?.updateLayoutParams<FrameLayout.LayoutParams> {
                                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                                height = WRAP_CONTENT
                            }
                        }
                }
            }

            "Banner With Refresh" -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val logger = ScreenAdLogger(identifier = item, logView = logs)
                    ads += Nimbus.bannerAd(
                        position = item,
                        size = Format.BANNER_320_50,
                        adPosition = Position.HEADER,
                        refreshInterval = 30,
                    )
                        .onEvent {
                            logger.onAdEvent(it)
                        }.onError {
                            logger.onError(it)
                        }.load(adFrame).show(adFrame).also {
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
                    }.load(adFrame).show(adFrame).also {
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
                    }.load(adFrame).show(adFrame).also {
                        it.adView?.updateLayoutParams<FrameLayout.LayoutParams> {
                            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                            height = WRAP_CONTENT
                        }
                    }
                }
            }

            "Interstitial Hybrid" -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val logger = ScreenAdLogger(identifier = item, logView = logs)
                    ads += Nimbus.interstitialAd(position = item) {
                        video()
                    }.onEvent {
                        logger.onAdEvent(it)
                    }.onError {
                        logger.onError(it)
                    }.show(requireContext(), closeButtonDelay = 10.seconds)
                }
            }

            "Interstitial Static" -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val logger = ScreenAdLogger(identifier = item, logView = logs)
                    ads += Nimbus.fullScreenAd(position = item) {
                        banner(size = Format.INTERSTITIAL_PORT)
                    }.onEvent {
                        logger.onAdEvent(it)
                    }.onError {
                        logger.onError(it)
                    }.show(requireContext())
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
                    }.show(requireContext())
                }
            }

            "Rewarded Video" -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val logger = ScreenAdLogger(identifier = item, logView = logs)
                    ads += Nimbus.rewardedAd(position = item).onEvent {
                        logger.onAdEvent(it)
                    }.onError {
                        logger.onError(it)
                    }.show(requireContext())
                }
            }

            "Ads in ScrollView" -> {
                LayoutAdsInListBinding.inflate(inflater, adFrame, true).apply {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val logger = ScreenAdLogger(identifier = item, logView = logs)

                        ads += Nimbus.bannerAd(
                            position = "$item Banner",
                            size = Format.BANNER_320_50,
                            refreshInterval = 30,
                            adPosition = Position.HEADER,
                        ).onEvent {
                            logger.onAdEvent(it)
                        }.onError {
                            logger.onError(it)
                        }.show(adFrameBanner)

                        ads += Nimbus.inlineAd(position = "$item Banner", refreshInterval = 30.seconds) {
                            banner(size = Format.INTERSTITIAL_PORT)
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
        RequestManager.interceptors.remove(this)
        ads.forEach { it.destroy() }
    }

    override fun modifyRequest(request: NimbusRequest) {
        request.request.imp[0].ext.facebook_app_id = null
        request.request.user?.ext = request.request.user?.ext?.apply {
            facebook_buyeruid = null
            unity_buyeruid = null
            mfx_buyerdata = null
            vungle_buyeruid = null
        }
    }
}
