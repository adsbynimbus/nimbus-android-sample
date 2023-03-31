package com.adsbynimbus.android.sample.mediation

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.databinding.FragmentGamDemoBinding
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.Renderer
import com.adsbynimbus.request.NimbusRequest
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MaxAdsFragment : Fragment() {

    private lateinit var adView: MaxAdView
    private var adController: AdController? = null
    val nimbusBidder = NimbusBidder { NimbusRequest.forBannerAd("test_banner", Format.BANNER_320_50, Position.HEADER) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentGamDemoBinding.inflate(inflater, container, false).apply {
        val bundle = requireArguments()
        headerView.setTitleText(bundle.getString("titleText", ""))
        headerView.setSubtitleText(bundle.getString("subtitleText", ""))
        adView = MaxAdView("YOUR_AD_UNIT_ID", requireContext())

        adView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50)
        adView.setBackgroundColor(Color.BLACK)

        lifecycleScope.launch {
            val maxBid = async { adView.auction() }
            val nimbusBid = async { nimbusBidder.fetchBid(requireContext()) }
            awaitAll(maxBid, nimbusBid)
            if (maxBid.getCompleted() > (nimbusBid.getCompleted()?.response?.bidRaw()?.toDouble() ?: 0.0)) {
                banner.addView(adView)
            } else {
                Renderer.loadAd(nimbusBid.getCompleted()!!.response!!, banner, object : NimbusAdManager.Listener {
                    override fun onAdRendered(controller: AdController) {
                        adController = controller
                    }
                })
            }
        }

    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        adView.destroy()
        adController?.destroy()
    }

    private suspend fun MaxAdView.auction(): Double = suspendCancellableCoroutine {
        setListener(object : MaxAdViewAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                it.resume(ad?.revenue ?: 0.0)
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                it.resume(0.0)
            }

            override fun onAdDisplayed(ad: MaxAd?) {  TODO("Not yet implemented") }

            override fun onAdHidden(ad: MaxAd?) { TODO("Not yet implemented") }

            override fun onAdClicked(ad: MaxAd?) { TODO("Not yet implemented") }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) { TODO("Not yet implemented") }

            override fun onAdExpanded(ad: MaxAd?) { TODO("Not yet implemented") }

            override fun onAdCollapsed(ad: MaxAd?) { TODO("Not yet implemented") }
        })
        loadAd()
    }
}
