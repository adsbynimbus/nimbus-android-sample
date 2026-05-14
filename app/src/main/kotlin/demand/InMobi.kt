package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.Gravity.TOP
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.ScreenAdLogger
import com.adsbynimbus.android.sample.rendering.disableAllExtensions
import com.adsbynimbus.extension.InMobiExtension
import com.adsbynimbus.rtb.Position.Header
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class InMobiFragment : Fragment() {

    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        disableAllExtensions()
        Nimbus.extensions<InMobiExtension>()?.enabled = true
        when (val item = requireArguments().getString("item")) {
            "Banner" -> viewLifecycleOwner.lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.bannerAd(position = item, size = AdSize.Banner, adPosition = Header)
                    .onEvent {
                        Log.i("Test3.0", "Event $it")
                        logger.onAdEvent(it)
                    }.onError {
                        Log.i("Test3.0", "Error $it")
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
                Nimbus.interstitialAd(position = item).onEvent {
                    Log.i("Test3.0", "Event $it")
                    logger.onAdEvent(it)
                }.onError {
                    Log.i("Test3.0", "Error $it")
                    logger.onError(it)
                }.show(this@InMobiFragment, closeButtonDelay = 10.seconds)
            }
            "Rewarded" -> viewLifecycleOwner.lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                Nimbus.rewardedAd(position = item).onEvent {
                    Log.i("Test3.0", "Event $it")
                    logger.onAdEvent(it)
                }.onError {
                    Log.i("Test3.0", "Error $it")
                    logger.onError(it)
                }.show(this@InMobiFragment, closeButtonDelay = 10.seconds)
            }
        }
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        ads.forEach { it.destroy() }
    }
}
