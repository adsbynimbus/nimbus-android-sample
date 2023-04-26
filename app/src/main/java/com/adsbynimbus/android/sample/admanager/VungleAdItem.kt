package com.adsbynimbus.android.sample.admanager

import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.common.Describable

enum class VungleAdItem(
    val adType: String,
    val gradlePropertyName: String,
    val placementId: String,
    override val description: String
) : Describable {
    BANNER(
        "static",
        "VUNGLE_BANNER",
        BuildConfig.VUNGLE_BANNER_320_ID,
        "Vungle Banner",
    ),

    MREC(
        "static",
        "VUNGLE_MREC_ID",
        BuildConfig.VUNGLE_MREC_ID,
        "Vungle MREC",
    ),

    INTERSTITIAL(
        "static",
        "VUNGLE_INTERSTITIAL_ID",
        BuildConfig.VUNGLE_INTERSTITIAL_ID,
        "Vungle Interstitial",
    ),
    REWARDED(
        "static",
        "VUNGLE_REWARDED_ID",
        BuildConfig.VUNGLE_REWARDED_ID,
        "Vungle Rewarded",
    );

    val isInterstitial get() = this == INTERSTITIAL
}
