package com.adsbynimbus.android.sample.mediation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.adManager
import com.adsbynimbus.android.sample.databinding.FragmentGamDemoBinding
import com.adsbynimbus.lineitem.applyDynamicPrice
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.NimbusResponse
import com.adsbynimbus.request.RequestManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import timber.log.Timber

class GAMDemoFragment : Fragment() {

    private val args: GAMDemoFragmentArgs by navArgs()
    private var adView: AdManagerAdView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentGamDemoBinding.inflate(inflater, container, false).apply {
        headerView.setTitleText(args.titleText)
        headerView.setSubtitleText(args.subtitleText)

        when (args.item) {
            MediationItem.BANNER -> banner.requestBannerAd()
            MediationItem.DYNAMIC_PRICE_BANNER -> banner.requestBannerAd(isDynamicPrice = true)
            MediationItem.INTERSTITIAL -> root.context.requestInterstitialAd()
            MediationItem.DYNAMIC_PRICE_INTERSTITIAL ->
                root.context.requestDynamicInterstitialAd()
            MediationItem.DYNAMIC_PRICE_INTERSTITIAL_BANNER ->
                root.context.requestDynamicInterstitialBannerAd()
            MediationItem.DYNAMIC_PRICE_INTERSTITIAL_VIDEO ->
                root.context.requestDynamicInterstitialVideoAd()
        }
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        adView?.destroy()
    }

    private fun FrameLayout.requestBannerAd(isDynamicPrice: Boolean = false) {
        adView = AdManagerAdView(requireContext()).apply {
            adUnitId = BuildConfig.GAM_PLACEMENT_ID
            setAdSizes(AdSize.BANNER)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Timber.v("Banner ad loaded")
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Timber.w("Error loading banner ad %s", p0.message)
                }
            }
        }
        addView(adView)

        val requestBuilder = AdManagerAdRequest.Builder()
        adManager.makeRequest(
            requireContext(),
            NimbusRequest.forBannerAd("test_banner", Format.BANNER_320_50, Position.HEADER),
            object : RequestManager.Listener {
                override fun onAdResponse(nimbusResponse: NimbusResponse) {
                    if (isDynamicPrice) {
                        requestBuilder.applyDynamicPrice(nimbusResponse) { "10" }
                    }
                    adView?.loadAd(requestBuilder.build())
                }
            }
        )
    }

    private fun Context.requestDynamicInterstitialAd() {
        adManager.makeRequest(this, NimbusRequest.forInterstitialAd("test_interstitial"),
            object : RequestManager.Listener {
                override fun onAdResponse(nimbusResponse: NimbusResponse) {
                    AdManagerInterstitialAd.load(this@requestDynamicInterstitialAd,
                        BuildConfig.GAM_PLACEMENT_ID,
                        AdManagerAdRequest.Builder().apply {
                            nimbusResponse.applyDynamicPrice(this) { "10" }
                        }.build(),
                        object : AdManagerInterstitialAdLoadCallback() {
                            override fun onAdLoaded(interstitialAd: AdManagerInterstitialAd) {
                                Timber.v("Interstitial ad loaded")
                                interstitialAd.show(requireActivity())
                            }

                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                Timber.w("Error loading interstitial ad %s", p0.message)
                            }
                        })
                }

                override fun onError(error: NimbusError) {
                    Timber.i("Error requesting ad from Nimbus")
                }
            }
        )
    }

    private fun Context.requestDynamicInterstitialBannerAd() {
        adManager.makeRequest(this, NimbusRequest.forInterstitialAd("test_interstitial").apply {
            request.imp[0].video = null
        }, object : RequestManager.Listener {
            override fun onAdResponse(nimbusResponse: NimbusResponse) {
                AdManagerInterstitialAd.load(this@requestDynamicInterstitialBannerAd,
                    BuildConfig.GAM_PLACEMENT_ID,
                    AdManagerAdRequest.Builder().apply {
                        nimbusResponse.applyDynamicPrice(this) { "10" }
                    }.build(),
                    object : AdManagerInterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: AdManagerInterstitialAd) {
                            Timber.v("Interstitial ad loaded")
                            interstitialAd.show(requireActivity())
                        }

                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            Timber.w("Error loading interstitial ad %s", p0.message)
                        }
                    })
            }
        })
    }

    private fun Context.requestDynamicInterstitialVideoAd() {
        adManager.makeRequest(this, NimbusRequest.forInterstitialAd("test_interstitial").apply {
            request.imp[0].banner = null
        }, object : RequestManager.Listener {
            override fun onAdResponse(nimbusResponse: NimbusResponse) {
                AdManagerInterstitialAd.load(this@requestDynamicInterstitialVideoAd,
                    BuildConfig.GAM_PLACEMENT_ID,
                    AdManagerAdRequest.Builder().apply {
                        nimbusResponse.applyDynamicPrice(this) { "10" }
                    }.build(),
                    object : AdManagerInterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: AdManagerInterstitialAd) {
                            Timber.v("Interstitial ad loaded")
                            interstitialAd.show(requireActivity())
                        }

                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            Timber.w("Error loading interstitial ad %s", p0.message)
                        }
                    })
            }
        })
    }

    private fun Context.requestInterstitialAd() {
        AdManagerInterstitialAd.load(this, BuildConfig.GAM_PLACEMENT_ID,
            AdManagerAdRequest.Builder().build(),
            object : AdManagerInterstitialAdLoadCallback() {
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
