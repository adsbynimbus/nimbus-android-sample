package com.adsbynimbus.android.sample.demand

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.disableAllExtensions
import com.adsbynimbus.android.sample.rendering.showPropertyMissingDialog
import com.adsbynimbus.request.AdSize
import com.facebook.ads.AdSettings
import com.facebook.ads.AdSettings.TestAdType
import kotlinx.coroutines.launch

/**
 * Initializes the Meta SDK and integrates it with the Nimbus SDK.
 *
 * @param appId the application id provided by Meta; can be derived from a placement id
 * @see appIdFromMetaPlacementId
 */
fun Context.initializeMetaAudienceNetwork(appId: String) {
    MetaExtension.initialize(this, appId)
    //AdSettings.addTestDevice(/* Add Test Device ID From Logcat here if necessary */)
}

/** Returns the app id derived from a placement id */
fun appIdFromMetaPlacementId(placement: String) = placement.substringBefore("_")

/**
 * This Fragment shows what Meta ads look like when run through the Nimbus renderer but is not
 * indicative of normal usage as the Nimbus server determines which ad units to request based on the
 * request sent from the client.
 */
class MetaFragment : Fragment() {

    val ads = mutableListOf<Ad>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {

        // Enabling Meta Ads test mode. Must not be set `true` in production.
        AdSettings.setTestMode(true)
        disableAllExtensions()
        Nimbus.extensions<MetaExtension>()?.enabled = true

        when (val item = requireArguments().getString("item")) {
            "Meta Banner" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.bannerAd(
                    mockMetaNimbusAdPosition(
                        item,
                        { requireActivity().showPropertyMissingDialog(it) },
                    ),
                    AdSize.BANNER,
                ).show(adFrame)
            }

            "Meta Native" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.bannerAd(
                    mockMetaNimbusAdPosition(
                        item,
                        { requireActivity().showPropertyMissingDialog(it) },
                    ),
                    AdSize.BANNER,
                ) {
                    native()
                }.show(adFrame)
            }

            "Meta Interstitial" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.interstitialAd(
                    mockMetaNimbusAdPosition(
                        item,
                        { requireActivity().showPropertyMissingDialog(it) },
                    ),
                ).show(this@MetaFragment)
            }

            "Meta Rewarded Video" -> viewLifecycleOwner.lifecycleScope.launch {
                ads += Nimbus.rewardedAd(
                    mockMetaNimbusAdPosition(
                        item,
                        { requireActivity().showPropertyMissingDialog(it) },
                    ),
                ).show(this@MetaFragment)
            }
        }
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        ads.forEach { it.destroy() }
    }
}

val bannerTypes = arrayOf(
    TestAdType.IMG_16_9_APP_INSTALL,
    TestAdType.IMG_16_9_LINK,
)
val interstitialTypes = bannerTypes + arrayOf(
    TestAdType.CAROUSEL_IMG_SQUARE_APP_INSTALL,
    TestAdType.CAROUSEL_IMG_SQUARE_LINK,
)

val rewardedTypes = arrayOf(
    TestAdType.VIDEO_HD_16_9_46S_APP_INSTALL,
    TestAdType.VIDEO_HD_16_9_15S_APP_INSTALL,
    TestAdType.VIDEO_HD_9_16_39S_APP_INSTALL,
    TestAdType.PLAYABLE,
)

val nativeTypes = interstitialTypes

fun mockMetaNimbusAdPosition(type: String, onPropertyMissing: (String) -> Unit) =
    when (type) {
        "Meta Native" -> nativeTypes.random().let {
            it.adTypeString + "#" + if (it in bannerTypes) BuildConfig.FAN_NATIVE_320_ID.also { id ->
                if (id.isEmpty()) onPropertyMissing("sample_fan_native_320_id")
            } else BuildConfig.FAN_NATIVE_ID.also { id ->
                if (id.isEmpty()) onPropertyMissing("sample_fan_native_id")
            }
        }

        "Meta Interstitial" -> "${interstitialTypes.random().adTypeString}#${
            BuildConfig.FAN_INTERSTITIAL_ID.also { id ->
                if (id.isEmpty()) onPropertyMissing("sample_fan_interstitial_id")
            }
        }"

        "Meta Rewarded Video" -> "${rewardedTypes.random().adTypeString}#${
            BuildConfig.FAN_REWARDED_VIDEO_ID.also { id ->
                if (id.isEmpty()) onPropertyMissing("sample_fan_rewarded_video_id")
            }
        }"

        else -> "${bannerTypes.random().adTypeString}#${
            BuildConfig.FAN_BANNER_320_ID.also { id ->
                if (id.isEmpty()) onPropertyMissing("sample_fan_banner_320_id")
            }
        }"
    }
