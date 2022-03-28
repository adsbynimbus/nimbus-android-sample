package com.adsbynimbus.android.sample.admanager

import com.adsbynimbus.android.sample.common.Describable

enum class AdItem(
    override val description: String
) : Describable {
    MANUAL_REQUEST_RENDER_AD("Manual Request/Render Ad"),
    BANNER("Banner"),
    INTERSTITIAL_STATIC("Interstitial Static"),
    INTERSTITIAL_VIDEO("Interstitial Video"),
    INTERSTITIAL_HYBRID("Interstitial Hybrid"),
    BLOCKING_INTERSTITIAL("Blocking Interstitial (5 sec)"),
    REWARDED_STATIC("Rewarded Static (5 sec)"),
    REWARDED_VIDEO("Rewarded Video (5 sec)"),
    REWARDED_VIDEO_UNITY("Rewarded Video Unity (5 sec)"),
}
