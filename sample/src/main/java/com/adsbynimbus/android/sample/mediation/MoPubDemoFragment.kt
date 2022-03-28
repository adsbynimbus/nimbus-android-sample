package com.adsbynimbus.android.sample.mediation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.adManager
import com.adsbynimbus.android.sample.databinding.FragmentMopubDemoBinding
import com.adsbynimbus.lineitem.applyDynamicPrice
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.NimbusResponse
import com.adsbynimbus.request.RequestManager
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.common.logging.MoPubLog
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import com.mopub.mobileads.MoPubView
import timber.log.Timber

class MoPubDemoFragment : Fragment(), MoPubInterstitial.InterstitialAdListener {

    private var interstitial: MoPubInterstitial? = null
    private val args: MoPubDemoFragmentArgs by navArgs()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (!MoPub.isSdkInitialized()) {
            MoPub.initializeSdk(
                context.applicationContext,
                SdkConfiguration.Builder(BuildConfig.MOPUB_BANNER_ID)
                    .withLogLevel(MoPubLog.LogLevel.DEBUG).build()
            ) {}
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentMopubDemoBinding.inflate(inflater, container, false).apply {
            headerView.setTitleText(args.titleText)
            headerView.setSubtitleText(args.subtitleText)

            when (args.item) {
                MediationItem.BANNER, MediationItem.DYNAMIC_PRICE_BANNER -> {
                    val isDynamicPrice = args.item == MediationItem.DYNAMIC_PRICE_BANNER
                    requestBannerAd(isDynamicPrice, banner)
                }
                MediationItem.INTERSTITIAL, MediationItem.DYNAMIC_PRICE_INTERSTITIAL -> {
                    val isDynamicPrice = args.item == MediationItem.DYNAMIC_PRICE_INTERSTITIAL
                    requestInterstitialAd(isDynamicPrice)
                }
                else -> return@apply
            }
        }.root

    private fun requestBannerAd(isDynamicPrice: Boolean, bannerAd: MoPubView) {
        bannerAd.autorefreshEnabled = false
        bannerAd.setAdUnitId(BuildConfig.MOPUB_BANNER_ID)
        bannerAd.adSize = MoPubView.MoPubAdSize.HEIGHT_50
        bannerAd.bannerAdListener = object : MoPubView.BannerAdListener {
            override fun onBannerLoaded(p0: MoPubView) {
                Timber.v("Banner ad loaded")
            }

            override fun onBannerFailed(p0: MoPubView?, p1: MoPubErrorCode?) {
                Timber.w("Error loading banner ad %s", p1?.toString())
            }

            override fun onBannerClicked(p0: MoPubView?) {}

            override fun onBannerExpanded(p0: MoPubView?) {}

            override fun onBannerCollapsed(p0: MoPubView?) {}
        }

        adManager.makeRequest(
            requireContext(),
            NimbusRequest.forBannerAd(
                "test_banner",
                Format.BANNER_320_50,
                Position.HEADER
            ),
            object : RequestManager.Listener {
                override fun onAdResponse(nimbusResponse: NimbusResponse) {
                    if (isDynamicPrice) {
                        bannerAd.applyDynamicPrice(nimbusResponse) { "50" }
                    }
                    bannerAd.loadAd()
                }
            }
        )
    }

    private fun requestInterstitialAd(isDynamicPrice: Boolean) {
        interstitial?.destroy()
        interstitial = MoPubInterstitial(
            requireActivity(),
            BuildConfig.MOPUB_INTERSTITIAL_ID
        ).apply {
            interstitialAdListener = this@MoPubDemoFragment
            if (isDynamicPrice) {
                adManager.makeRequest(
                    requireContext(),
                    NimbusRequest.forInterstitialAd("test_interstitial"),
                    object : RequestManager.Listener {
                        override fun onAdResponse(nimbusResponse: NimbusResponse) {
                            nimbusResponse.applyDynamicPrice(this@apply)
                        }

                        override fun onError(error: NimbusError?) {
                            super.onError(error)
                        }
                    }
                )
            } else {
                load()
            }
        }
    }

    override fun onDestroyView() {
        FragmentMopubDemoBinding.bind(requireView()).banner.destroy()
        interstitial?.destroy()
        interstitial = null
        super.onDestroyView()
    }

    override fun onInterstitialLoaded(interstitial: MoPubInterstitial?) {
        Timber.v("Interstitial ad loaded")
        interstitial?.show()
    }

    override fun onInterstitialShown(interstitial: MoPubInterstitial?) {}

    override fun onInterstitialFailed(
        interstitial: MoPubInterstitial?,
        errorCode: MoPubErrorCode?
    ) {
        Timber.w("Error loading interstitial ad %s", errorCode?.toString())
    }

    override fun onInterstitialDismissed(interstitial: MoPubInterstitial?) {}

    override fun onInterstitialClicked(interstitial: MoPubInterstitial?) {}
}
