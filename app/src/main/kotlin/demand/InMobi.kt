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
import com.adsbynimbus.android.sample.BuildConfig.INMOBI_ACCOUNT_ID
import com.adsbynimbus.android.sample.databinding.InmobiNativeAdBinding
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.ScreenAdLogger
import com.adsbynimbus.android.sample.rendering.disableAllExtensions
import com.adsbynimbus.openrtb.enumerations.Position.HEADER
import com.adsbynimbus.openrtb.request.Format.Companion.BANNER_320_50
import com.inmobi.ads.InMobiNative
import com.inmobi.sdk.InMobiSdk
import com.inmobi.sdk.SdkInitializationListener
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds


fun initializeInMobi(context: Context, accountId: String) {
    if (InMobiSdk.isSDKInitialized()) return

    InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG)
    val consentObject = JSONObject()
    // Provide correct consent value to sdk which is obtained by User
    consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true)
    // Provide 0 if GDPR is not applicable and 1 if applicable
    consentObject.put("gdpr", "0")
    // Provide user consent in IAB format
//        consentObject.put(InMobiSdk.IM_GDPR_CONSENT_IAB, )

    InMobiSdk.init(
        context, accountId, consentObject,
        object : SdkInitializationListener {
            override fun onInitializationComplete(error: Error?) {
                Timber.w("InMobi SDK initialized with ${error?.message}")
            }
        },
    )
}

class InMobiFragment : Fragment() {

    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        initializeInMobi(requireContext(), INMOBI_ACCOUNT_ID)
        disableAllExtensions()
        Nimbus.extensions<InMobiExtension>()?.enabled = true
        when (val item = requireArguments().getString("item")) {
            "Banner" -> viewLifecycleOwner.lifecycleScope.launch {
                val logger = ScreenAdLogger(identifier = item, logView = logs)
                ads += Nimbus.bannerAd(position = item, size = BANNER_320_50, adPosition = HEADER)
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
                    ads += Nimbus.nativeAd(position = item, size = BANNER_320_50)
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
