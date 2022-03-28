package com.adsbynimbus.android.sample

import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.request.NimbusRequest

val adManager: NimbusAdManager = NimbusAdManager()

fun forVideoAd(
    position: String = "test_video",
    fullScreen: Boolean = false
): NimbusRequest = NimbusRequest.forRewardedVideo(position).apply {
    request.imp[0].instl = if (fullScreen) 1 else 0
}
