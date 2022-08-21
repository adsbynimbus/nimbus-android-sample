package com.adsbynimbus.android.sample.admanager

import com.adsbynimbus.NimbusAd
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.common.Describable
import com.facebook.ads.AdSettings.TestAdType

enum class FANAdItem(
    val adType: String,
    val testAdTypes: Array<TestAdType>,
    val gradlePropertyName: String,
    override val description: String
) : Describable {
    BANNER(
        "static",
        arrayOf(TestAdType.IMG_16_9_APP_INSTALL, TestAdType.IMG_16_9_LINK),
        "FAN_BANNER_320_ID",
        "Facebook Banner",
    ),

    INTERSTITIAL(
        "static",
        arrayOf(
            TestAdType.IMG_16_9_APP_INSTALL,
            TestAdType.IMG_16_9_LINK,
            TestAdType.CAROUSEL_IMG_SQUARE_APP_INSTALL,
            TestAdType.CAROUSEL_IMG_SQUARE_LINK
        ),
        "FAN_INTERSTITIAL_ID",
        "Facebook Interstitial",
    ),

    NATIVE(
        "native",
        INTERSTITIAL.testAdTypes,
        "FAN_NATIVE_ID and/or FAN_NATIVE_320_ID",
        "Facebook Native",
    );

    val isInterstitial get() = this == INTERSTITIAL
}

fun forFacebookNative() = object : NimbusAd {
    override fun placementId(): String = FANAdItem.NATIVE.placementId(TestAdType.IMG_16_9_LINK)

    override fun type(): String = FANAdItem.NATIVE.adType

    override fun network(): String = "facebook"

    override fun isInterstitial(): Boolean = false

    override fun markup(): String = ""
}

fun FANAdItem.placementId(adType: TestAdType) = when (this) {
    FANAdItem.BANNER -> when (adType) {
        TestAdType.IMG_16_9_LINK,
        TestAdType.IMG_16_9_APP_INSTALL -> BuildConfig.FAN_BANNER_320_ID
        else -> null
    }
    FANAdItem.INTERSTITIAL -> BuildConfig.FAN_INTERSTITIAL_ID
    FANAdItem.NATIVE -> when (adType) {
        TestAdType.IMG_16_9_LINK,
        TestAdType.IMG_16_9_APP_INSTALL -> BuildConfig.FAN_NATIVE_320_ID
        else -> BuildConfig.FAN_NATIVE_ID
    }
}.let { "$adType#$it" }
