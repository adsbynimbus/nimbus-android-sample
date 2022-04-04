package com.adsbynimbus.android.sample.admanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.adManager
import com.adsbynimbus.android.sample.databinding.FragmentAdManagerBinding
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.AdEvent
import com.adsbynimbus.render.Renderer
import com.adsbynimbus.request.*
import timber.log.Timber

class AdManagerFragment : Fragment(), NimbusRequest.Interceptor {

    private var adController: AdController? = null
    private val args: AdManagerFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentAdManagerBinding.inflate(inflater, container, false).apply {
        headerView.setTitleText(args.titleText)
        headerView.setSubtitleText(args.subtitleText)

        when (args.item) {
            AdItem.MANUAL_REQUEST_RENDER_AD -> {
                // Manual Request Ad
                val request = NimbusRequest.forBannerAd(
                    "test_manual_request_banner",
                    Format.BANNER_320_50,
                    Position.HEADER,
                )

                // Manual Render Ad
                adManager.makeRequest(root.context, request, object : RequestManager.Listener {
                    override fun onAdResponse(nimbusResponse: NimbusResponse) {
                        // Render ad with response
                        Renderer.loadAd(nimbusResponse, adFrame,
                            object : Renderer.Listener, NimbusError.Listener {
                                override fun onAdRendered(controller: AdController) {
                                    adController = controller.apply {
                                        addListener("Manual Request/Render Controller")
                                    }
                                }

                                override fun onError(error: NimbusError) {
                                    Timber.e("Manual Render Ad: %s", error.message)
                                }
                            }
                        )
                    }

                    override fun onError(error: NimbusError) {
                        Timber.e("Manual Render Ad: %s", error.message)
                    }
                })
            }
            AdItem.BANNER -> adManager.showAd(
                NimbusRequest.forBannerAd("test_banner", Format.BANNER_320_50, Position.HEADER),
                adFrame,
            ) {
                adController = it.apply { addListener("Banner Controller") }
            }
            AdItem.INTERSTITIAL_STATIC -> adManager.showBlockingAd(
                NimbusRequest.forInterstitialAd("test_interstitial_static").apply {
                    request.imp[0].video = null
                },
                0,
                requireActivity(),
            ) {
                adController = it.apply { addListener("Interstitial Static Controller") }
            }
            AdItem.INTERSTITIAL_VIDEO -> adManager.showBlockingAd(
                NimbusRequest.forInterstitialAd("test_interstitial_video").apply {
                    request.imp[0].banner = null
                },
                0,
                requireActivity(),
            ) {
                adController = it.apply { addListener("Interstitial Video Controller") }
            }
            AdItem.INTERSTITIAL_HYBRID -> adManager.showBlockingAd(
                NimbusRequest.forInterstitialAd("test_interstitial_video"),
                0,
                requireActivity(),
            ) {
                adController = it.apply { addListener("Interstitial Hybrid Controller") }
            }
            AdItem.BLOCKING_INTERSTITIAL -> adManager.showBlockingAd(
                NimbusRequest.forInterstitialAd("test_blocking_interstitial"),
                requireActivity(),
            ) {
                it.addListener("Blocking Interstitial Controller")
            }
            AdItem.REWARDED_STATIC -> adManager.showRewardedAd(
                NimbusRequest.forInterstitialAd("test_rewarded_static"),
                5,
                requireActivity(),
            ) {
                it.addListener("Rewarded Static Controller")
            }
            AdItem.REWARDED_VIDEO -> adManager.showRewardedAd(
                NimbusRequest.forRewardedVideo("test_rewarded_video"),
                5,
                requireActivity(),
            ) {
                it.addListener("Rewarded Video Controller")
            }
            AdItem.REWARDED_VIDEO_UNITY -> adManager.showRewardedAd(
                NimbusRequest.forRewardedVideo("Rewarded_Android"),
                5,
                requireActivity(),
            ) {
                it.addListener("Rewarded Video Controller (Unity)")
            }
        }
    }.root

    override fun onStart() {
        super.onStart()
        RequestManager.interceptors.add(this)
    }

    override fun onStop() {
        super.onStop()
        RequestManager.interceptors.remove(this)
    }

    override fun onDestroyView() {
        adController?.destroy()
        adController = null
        super.onDestroyView()
    }

    override fun modifyRequest(request: NimbusRequest) {
        if (args.item == AdItem.REWARDED_VIDEO_UNITY) {
            request.request.imp[0].ext.facebook_app_id = ""
        }
        request.request.user = request.request.user?.apply {
            if (!FANDemandProvider.forceTestAd) buyeruid = null
            if (args.item != AdItem.REWARDED_VIDEO_UNITY) ext?.unity_buyeruid = null
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
