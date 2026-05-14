package com.adsbynimbus.android.sample.rendering

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.databinding.LayoutAdsInListBinding
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.internal.application
import com.adsbynimbus.rtb.Position
import com.adsbynimbus.render.NimbusAdView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class AdFormatsFragment : Fragment() {

    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        disableAllExtensions()
        when (val item = requireArguments().getString("item")) {
            "Banner" -> lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.bannerAd(position = item, size = AdSize.Banner, adPosition = Position.Header)
                    .onEvent {
                        logger.onAdEvent(it)
                        Log.i("Test3.0", "Event $it")
                    }.onError {
                        logger.onError(it)
                    }.show(adFrame).also {
                        it.adView?.updateLayoutParams<FrameLayout.LayoutParams> {
                            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                            height = WRAP_CONTENT
                        }
                        val nimbusView = (it.adView as NimbusAdView)
                        Log.i("Test3.0", "Exposure: ${nimbusView.exposure}")
                        delay(1000)
                        Log.i("Test3.0", "Exposure: ${nimbusView.exposure}")
                        delay(1000)
                        val obstruction = View(context)
                        Log.i("Test3.0", "Adding Obstruction")
                        adFrame.addView(obstruction, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
                        delay(2000)
                        Log.i("Test3.0", "Marking Friendly Obstruction")
                        it.friendlyObstructions = listOf(FriendlyObstruction(obstruction,
                            FriendlyObstruction.Purpose.NotVisible, ""))
                        delay(2000)
                        Log.i("Test3.0", "Exposure ${nimbusView.exposure}")
                    }
            }

            "Banner With Refresh" -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val logger = ScreenAdLogger(identifier = item, logView = logs)
                    Nimbus.bannerAd(
                        position = item,
                        size = AdSize.Banner,
                        adPosition = Position.Header,
                        refreshInterval = 30,
                    ).onEvent {
                        logger.onAdEvent(it)
                        Log.i("Test3.0", "Event $it")
                    }.onError {
                        logger.onError(it)
                    }.show(adFrame).also {
                        it.adView?.updateLayoutParams<FrameLayout.LayoutParams> {
                            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                            height = WRAP_CONTENT
                        }
                        delay(2000)
                        it.show(adFrame)
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
                        Log.i("Test3.0", "Event $it")
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
                        Log.i("Test3.0", "Event $it")
                        logger.onAdEvent(it)
                    }.onError {
                        logger.onError(it)
                    }.show(adFrame).also {
                        it.adView?.updateLayoutParams<FrameLayout.LayoutParams> {
                            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                            height = WRAP_CONTENT
                        }
                        delay(2000)
                        Log.i("Test3.0", "Calling Fetch - id:${it.hashCode()}")
                        it.fetch()
                        delay(2000)
                        Log.i("Test3.0", "Calling Show - id:${it.hashCode()}")
                        it.show(adFrame)
                        Log.i("Test3.0", "Calling Destroy - id:${it.hashCode()}")
                        it.destroy()
                    }
                }
            }

            "Interstitial Hybrid" -> {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                Nimbus.interstitialAd(position = item).onEvent {
                    Log.i("Test3.0", "Event $it")
                }.onError {
                }.apply {
                    lifecycleScope.launch {
                        Log.i("Test3.0", "Calling show")
                        show(requireActivity())
                        Log.i("Test3.0", "Ad Type: ${response?.bid?.mtype?.name}")
                        delay(2000)
                        Log.i("Test3.0", "Calling Load")
                        load(application)
                        delay(2000)
                        Log.i("Test3.0", "Calling Show")
                        show(requireActivity())
                        delay(2000)
                        Log.i("Test3.0", "Calling Destroy")
                        destroy()
                    }
                }

            }
            "Interstitial Static" -> {
                ads += Nimbus.fullscreenAd(position = item) {
                    banner(size = AdSize.InterstitialPortrait)
                }.onEvent {
                    Log.i("Test3.0", "Event $it")
                }.onError {
                    //logger.onError(it)
                }.apply {
                    lifecycleScope.launch {
                        show(this@AdFormatsFragment, closeButtonDelay = 10.seconds)
                    }
                }
            }

            "Interstitial Video" -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val logger = ScreenAdLogger(identifier = item, logView = logs)
                    Nimbus.fullscreenAd(position = item) {
                        video()
                    }.onEvent {
                        Log.i("Test3.0", "Event $it")
                        logger.onAdEvent(it)
                    }.onError {
                        Log.i("Test3.0", "Error $it")
                        logger.onError(it)
                    }.apply {
                        Log.i("Test3.0", "Calling show")
                        show(requireActivity())
                        Log.i("Test3.0", "Ad Type: ${response?.bid?.mtype?.name}")
                        delay(2000)
                        Log.i("Test3.0", "Calling Load")
                        load(application)
                        delay(2000)
                    }
                }
            }

            "Rewarded Video" -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    val logger = ScreenAdLogger(identifier = item, logView = logs)
                    Nimbus.rewardedAd(position = item).onEvent {
                        logger.onAdEvent(it)
                    }.onError {
                        logger.onError(it)
                    }.show(this@AdFormatsFragment)
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
