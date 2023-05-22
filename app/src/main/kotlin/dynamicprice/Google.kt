package com.adsbynimbus.android.sample.dynamicprice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.allViews
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.test.showPropertyMissingDialog
import com.adsbynimbus.lineitem.LinearPriceGranularity
import com.adsbynimbus.lineitem.LinearPriceMapping
import com.adsbynimbus.lineitem.applyDynamicPrice
import com.adsbynimbus.openrtb.enumerations.Apis
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.openrtb.request.Video
import com.adsbynimbus.render.NimbusAdView
import com.adsbynimbus.request.NimbusRequest
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.System.currentTimeMillis

class GoogleAdManagerDynamicPriceFragment : Fragment() {

    val nimbusAdManager = NimbusAdManager()

    /** Sets Google as the Viewability Partner for Nimbus ads rendered by Google and make the request */
    suspend fun NimbusAdManager.makeDynamicPriceRequest(request: NimbusRequest) = makeRequest(requireContext(),
        request = request.apply {
            configureViewability(
                partnerName = "Google",
                partnerVersion = MobileAds.getVersion().toString(),
            )
        }
    )

    fun createAdManagerAdView() = AdManagerAdView(requireContext()).apply {
        adUnitId = BuildConfig.GAM_PLACEMENT_ID
        id = R.id.google_ad_view
        adListener = object : AdListener() {
            override fun onAdLoaded() {
                Timber.v("Banner ad loaded")
                addTestIds()
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                Timber.w("Error loading banner ad ${p0.message}")
            }
        }
    }

    /** This price mapping is an example, please confirm with your ad ops team what the actual values should be */
    val priceMapping = LinearPriceMapping(
        LinearPriceGranularity(0, 100, 1),   /* 0 cents to 1 dollar in 1 cent increments   */
        LinearPriceGranularity(100, 500, 5), /* 1 dollar to 5 dollars in 5 cent increments */
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        when (val item = requireArguments().getString("item")) {
            "Dynamic Price Banner" -> lifecycleScope.launch {
                val adView = createAdManagerAdView().apply {
                    setAdSizes(AdSize.BANNER)
                    adFrame.addView(this)
                }
                var lastRequestTime = 0L
                try {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        /* The while loop here enables refreshing the ad tied to the lifecycle */
                        while (isActive) {
                            delay(30_000 - (currentTimeMillis() - lastRequestTime))
                            lastRequestTime = currentTimeMillis()
                            val adRequest = AdManagerAdRequest.Builder()
                            runCatching {
                                nimbusAdManager.makeDynamicPriceRequest(
                                    request = NimbusRequest.forBannerAd(item, Format.BANNER_320_50, Position.HEADER)
                                )
                            }.onSuccess { response ->
                                response.applyDynamicPrice(request = adRequest, mapping = priceMapping)
                            }
                            adView.loadAd(adRequest.build())
                        }
                    }
                } finally {
                    adView.destroy()
                }
            }
            "Dynamic Price Banner + Video" -> lifecycleScope.launch {
                val adView = createAdManagerAdView().apply {
                    setAdSizes(AdSize(400, 300), AdSize.MEDIUM_RECTANGLE)
                    adFrame.addView(this)
                }
                var lastRequestTime = 0L
                try {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        /* The while loop here enables refreshing the ad tied to the lifecycle */
                        while (isActive) {
                            delay(30_000 - (currentTimeMillis() - lastRequestTime))
                            lastRequestTime = currentTimeMillis()
                            val adRequest = AdManagerAdRequest.Builder()
                            runCatching {
                                nimbusAdManager.makeDynamicPriceRequest(
                                    request = NimbusRequest.forBannerAd(item, Format.MREC).apply {
                                        request.imp[0].video = Video(
                                            protocols = NimbusRequest.defaultProtocols,
                                            api = byteArrayOf(Apis.OMID),
                                        )
                                    }
                                )
                            }.onSuccess { response ->
                                response.applyDynamicPrice(request = adRequest, mapping = priceMapping)
                            }
                            adView.loadAd(adRequest.build())
                        }
                    }
                } finally {
                    adView.destroy()
                }
            }
            "Dynamic Price Interstitial" -> lifecycleScope.launch {
                val adRequest = AdManagerAdRequest.Builder()
                runCatching {
                    nimbusAdManager.makeDynamicPriceRequest(request = NimbusRequest.forInterstitialAd(item))
                }.onSuccess { response ->
                    response.applyDynamicPrice(request = adRequest, mapping = priceMapping)
                }
                AdManagerInterstitialAd.load(requireActivity(), BuildConfig.GAM_PLACEMENT_ID,
                    adRequest.build(), object : AdManagerInterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: AdManagerInterstitialAd) {
                            Timber.v("Interstitial ad loaded")
                            interstitialAd.show(requireActivity())
                        }

                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            Timber.w("Error loading interstitial ad %s", p0.message)
                        }
                    }
                )
            }
        }
        if (BuildConfig.GAM_PLACEMENT_ID.isEmpty()) context?.showPropertyMissingDialog("sample_gam_placement_id")
    }.root

    private fun AdManagerAdView.addTestIds() {
        allViews.filterIsInstance<NimbusAdView>().forEach {
            it.id = R.id.nimbus_ad_view
            it.contentDescription = "test_demand static ad"
        }
    }
}
