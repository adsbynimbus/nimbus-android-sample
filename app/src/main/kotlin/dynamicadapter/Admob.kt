package com.adsbynimbus.android.sample.dynamicadapter

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
import com.adsbynimbus.android.sample.rendering.showPropertyMissingDialog
import com.adsbynimbus.google.NimbusCustomAdapter
import com.adsbynimbus.google.nimbusExtras
import com.adsbynimbus.google.notifyError
import com.adsbynimbus.google.notifyImpression
import com.adsbynimbus.google.notifyPrice
import com.adsbynimbus.render.NimbusAdView
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.RequestManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.launch
import timber.log.Timber

public class GoogleAdMobDynamicFragment : Fragment() {
    
    private val adManager = NimbusAdManager()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
         RequestManager.interceptors.add(NimbusRequest.Interceptor {
            it.request.imp[0].ext.facebook_app_id = ""
            it.request.user?.ext = it.request.user?.ext?.apply {
                facebook_buyeruid = null
                unity_buyeruid = null
                mfx_buyerdata = null
                vungle_buyeruid = null
            }
        })
        val item = requireArguments().getString("item")
        val nimbusExtras = nimbusExtras {
            position = item
        }

        when (item) {
            "Dynamic Banner" -> lifecycleScope.launch {
                AdView(requireContext()).apply {
                    adUnitId = BuildConfig.ADMOB_BANNER
                    id = R.id.google_ad_view
                    setAdSize(AdSize.BANNER)

                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Timber.v("Banner ad loaded")
                            addTestIds()
                        }

                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            Timber.w("Error loading banner ad ${p0.message}")
                            adManager.notifyError(nimbusExtras, p0)
                        }

                        override fun onAdImpression() {
                            adManager.notifyImpression(nimbusExtras, responseInfo)
                        }
                    }
                    onPaidEventListener = OnPaidEventListener {
                        adManager.notifyPrice(nimbusExtras, it)
                    }

                    adFrame.addView(
                        this, ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    )

                    try {
                        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                            loadAd(AdRequest.Builder().addNetworkExtrasBundle(NimbusCustomAdapter::class.java, nimbusExtras).build())
                        }
                    } finally {
                        destroy()
                    }
                }
            }

            "Dynamic Interstitial" -> AdManagerInterstitialAd.load(requireActivity(),
                BuildConfig.ADMOB_INTERSTITIAL,
                AdManagerAdRequest.Builder()
                    .addNetworkExtrasBundle(NimbusCustomAdapter::class.java, nimbusExtras)
                    .build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        Timber.v("Interstitial ad loaded")
                        ad.onPaidEventListener = OnPaidEventListener {
                            NimbusAdManager().notifyPrice(nimbusExtras, it)
                        }

                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdImpression() {
                                adManager.notifyImpression(nimbusExtras, ad.responseInfo)
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                adManager.notifyError(nimbusExtras, p0)
                            }
                        }

                        ad.show(requireActivity())
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        Timber.w("Error loading interstitial ad %s", p0.message)
                        adManager.notifyError(nimbusExtras, p0)
                    }
                }
            )

            "Dynamic Rewarded" -> RewardedAd.load(requireActivity(),
                BuildConfig.ADMOB_REWARDED,
                AdManagerAdRequest.Builder().addNetworkExtrasBundle(NimbusCustomAdapter::class.java, nimbusExtras).build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        Timber.v("Video rewarded ad loaded")

                        ad.onPaidEventListener = OnPaidEventListener {
                            NimbusAdManager().notifyPrice(nimbusExtras, it)
                        }

                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdImpression() {
                                adManager.notifyImpression(nimbusExtras, ad.responseInfo)
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                adManager.notifyError(nimbusExtras, p0)
                            }
                        }

                        ad.show(requireActivity()) {
                            Timber.v("Video rewarded ${it.amount} ${it.type}")
                        }
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        Timber.w("Error loading Rewarded Video ad %s", p0.message)
                        adManager.notifyError(nimbusExtras, p0)
                    }
                }
            )

            "Dynamic Rewarded Interstitial" -> RewardedInterstitialAd.load(requireActivity(),
                BuildConfig.ADMOB_REWARDED_INTERSTITIAL,
                AdManagerAdRequest.Builder().addNetworkExtrasBundle(NimbusCustomAdapter::class.java, nimbusExtras).build(),
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        Timber.v("Rewarded ad loaded")
                        ad.onPaidEventListener = OnPaidEventListener {
                            NimbusAdManager().notifyPrice(nimbusExtras, it)
                        }

                        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdImpression() {
                                adManager.notifyImpression(nimbusExtras, ad.responseInfo)
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                adManager.notifyError(nimbusExtras, p0)
                            }
                        }

                        ad.show(requireActivity()) {
                            Timber.v("rewarded ${it.amount} ${it.type}")
                        }
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        Timber.w("Error loading Rewarded ad %s", p0.message)
                        adManager.notifyError(nimbusExtras, p0)
                    }
                }
            )
        }
        if (BuildConfig.GAM_PLACEMENT_ID.isEmpty()) context?.showPropertyMissingDialog("sample_gam_placement_id")
    }.root

    private fun AdView.addTestIds() {
        allViews.filterIsInstance<NimbusAdView>().forEach {
            it.id = R.id.nimbus_ad_view
            it.contentDescription = "test_demand static ad"
        }
    }

}
