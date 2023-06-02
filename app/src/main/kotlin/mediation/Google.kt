package com.adsbynimbus.android.sample.mediation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.view.allViews
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.rendering.showPropertyMissingDialog
import com.adsbynimbus.render.NimbusAdView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import kotlinx.coroutines.launch
import timber.log.Timber

class GoogleAdManagerYieldGroupFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        when (requireArguments().getString("item")) {
            "Banner" -> lifecycleScope.launch {
                AdManagerAdView(requireContext()).apply {
                    adUnitId = BuildConfig.GAM_PLACEMENT_ID
                    id = R.id.google_ad_view
                    setAdSizes(AdSize.BANNER)
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Timber.v("Banner ad loaded")
                            addTestIds()
                        }

                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            Timber.w("Error loading banner ad ${p0.message}")
                        }
                    }

                    adFrame.addView(this, ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

                    try {
                        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                            loadAd(AdManagerAdRequest.Builder().build())
                        }
                    } finally {
                        destroy()
                    }
                }
            }
            "Interstitial" -> AdManagerInterstitialAd.load(requireActivity(), BuildConfig.GAM_PLACEMENT_ID,
                AdManagerAdRequest.Builder().build(), object : AdManagerInterstitialAdLoadCallback() {
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
        if (BuildConfig.GAM_PLACEMENT_ID.isEmpty()) context?.showPropertyMissingDialog("sample_gam_placement_id")
    }.root

    private fun AdManagerAdView.addTestIds() {
        allViews.filterIsInstance<NimbusAdView>().forEach {
            it.id = R.id.nimbus_ad_view
            it.contentDescription = "test_demand static 320x50"
        }
    }
}
