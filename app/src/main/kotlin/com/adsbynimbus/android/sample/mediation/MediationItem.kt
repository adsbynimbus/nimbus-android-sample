package com.adsbynimbus.android.sample.mediation

import com.adsbynimbus.android.sample.common.Describable

enum class MediationItem(
    override val description: String
) : Describable {
    BANNER("Banner"),
    DYNAMIC_PRICE_BANNER("Dynamic Price Banner"),
    INTERSTITIAL("Interstitial"),
    DYNAMIC_PRICE_INTERSTITIAL("Dynamic Price Interstitial"),
    DYNAMIC_PRICE_INTERSTITIAL_STATIC("Dynamic Price Interstitial Static"),
    DYNAMIC_PRICE_INTERSTITIAL_VIDEO("Dynamic Price Interstitial Video"),
}
