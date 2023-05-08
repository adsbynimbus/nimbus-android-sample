package com.adsbynimbus.android.sample.admanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.adManager
import com.adsbynimbus.android.sample.admanager.APSAdItem.BANNER
import com.adsbynimbus.android.sample.admanager.APSAdItem.INTERSTITIAL_HYBRID
import com.adsbynimbus.android.sample.request.loadAd
import com.adsbynimbus.android.sample.databinding.FragmentAdManagerBinding
import com.adsbynimbus.android.sample.request.DTBException
import com.adsbynimbus.android.sample.request.loadAll
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.AdEvent
import com.adsbynimbus.request.*
import com.amazon.device.ads.DTBAdRequest
import com.amazon.device.ads.DTBAdSize
import kotlinx.coroutines.launch
import timber.log.Timber

class APSDemoFragment : Fragment(), NimbusRequest.Interceptor {

    private var adController: AdController? = null
    private lateinit var item: APSAdItem

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentAdManagerBinding.inflate(inflater, container, false).apply {
        RequestManager.interceptors.add(this@APSDemoFragment)
        val bundle = requireArguments()
        headerView.setTitleText(bundle.getString("titleText", ""))
        headerView.setSubtitleText(bundle.getString("subtitleText", ""))
        item = bundle.getSerializable("item") as APSAdItem
        when (item) {
            BANNER -> lifecycleScope.launch {
                val nimbusRequest = NimbusRequest.forBannerAd("test_banner", Format.BANNER_320_50)
                val apsRequest = DTBAdRequest().apply {
                    setSizes(DTBAdSize(320, 50, BuildConfig.APS_BANNER))
                }

                /* See com.adsbynimbus.android.sample.request.Amazon.kt for implementation */
                runCatching { apsRequest.loadAd() }
                    .onSuccess { apsResponse ->
                        nimbusRequest.addApsResponse(apsResponse)
                        /* For refreshing banner requests */
                        nimbusRequest.addApsLoader(apsResponse.adLoader)
                    }.onFailure {
                        /* Add the loader from the AdError for refreshing banners */
                        if (it is DTBException) nimbusRequest.addApsLoader(it.error.adLoader)
                    }

                /* Show a Nimbus refreshing banner attached to the adFrame */
                adManager.showAd(nimbusRequest, refreshInterval = 30, viewGroup = adFrame) {
                    adController = it.apply { addListener("Banner Controller") }
                }
            }
            INTERSTITIAL_HYBRID -> lifecycleScope.launch {
                val nimbusRequest = NimbusRequest.forInterstitialAd("test_interstitial_with_aps")
                val apsInterstitial = DTBAdRequest().apply {
                    setSizes(DTBAdSize.DTBInterstitialAdSize(BuildConfig.APS_STATIC))
                }
                val apsVideo = DTBAdRequest().apply {
                    setSizes(DTBAdSize.DTBVideo(
                        resources.displayMetrics.widthPixels,
                        resources.displayMetrics.heightPixels, BuildConfig.APS_VIDEO))
                }

                /* See com.adsbynimbus.android.sample.request.Amazon.kt for implementation */
                listOf(apsInterstitial, apsVideo).loadAll().forEach { apsResponse ->
                    nimbusRequest.addApsResponse(apsResponse)
                }

                /* Show a Nimbus Interstitial ad with Display and Video in the same auction */
                adManager.showBlockingAd(nimbusRequest, requireActivity()) {
                    adController = it.apply { addListener("Interstitial Hybrid Controller") }
                }
            }
        }
    }.root

    override fun onDestroyView() {
        adController?.destroy()
        adController = null
        RequestManager.interceptors.remove(this)
        super.onDestroyView()
    }

    override fun modifyRequest(request: NimbusRequest) {
        request.request.imp[0].ext.facebook_app_id = ""
        request.request.user?.ext = request.request.user?.ext?.apply {
            facebook_buyeruid = null
            unity_buyeruid = null
            vungle_buyeruid = null
        }
    }

    private fun AdController.addListener(controllerName: String) {
        listeners().add(object : AdController.Listener {
            override fun onAdEvent(adEvent: AdEvent) {
                Timber.i("$controllerName: %s", adEvent.name)
                if (adEvent == AdEvent.DESTROYED) adController = null
            }

            override fun onError(error: NimbusError) {
                Timber.e("$controllerName: %s", error.message)
                adController = null
            }
        })
    }
}
