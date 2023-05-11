package com.adsbynimbus.android.sample.rendering

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.R.id.nimbus_ad_view
import com.adsbynimbus.android.sample.databinding.LayoutAdsInListBinding
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.test.LoggingAdControllerListener
import com.adsbynimbus.android.sample.test.NimbusAdManagerTestListener
import com.adsbynimbus.android.sample.test.testDescription
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.Renderer
import com.adsbynimbus.request.*
import timber.log.Timber

class AdManagerFragment : Fragment(), NimbusRequest.Interceptor {

    val adManager: NimbusAdManager = NimbusAdManager()
    private var adController: AdController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        RequestManager.interceptors.add(this@AdManagerFragment)
        when (val item = requireArguments().getString("item")) {
            "Manually Rendered Ad" -> adManager.makeRequest(
                context = root.context,
                request = NimbusRequest.forBannerAd(item, Format.BANNER_320_50, Position.HEADER),
                listener = object : RequestManager.Listener {
                override fun onAdResponse(nimbusResponse: NimbusResponse) {
                    // Render ad with response
                    Renderer.loadAd(nimbusResponse, adFrame,
                        object : Renderer.Listener, NimbusError.Listener {
                            override fun onAdRendered(controller: AdController) {
                                adController = controller.apply {
                                    view?.contentDescription = nimbusResponse.testDescription
                                    view?.id = nimbus_ad_view
                                    controller.align { Gravity.TOP or Gravity.CENTER_HORIZONTAL }
                                    controller.listeners.add(LoggingAdControllerListener(identifier = item))
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
            "Banner" -> adManager.showAd(
                request = NimbusRequest.forBannerAd(item, Format.BANNER_320_50, Position.HEADER),
                refreshInterval = 30,
                viewGroup = adFrame,
                listener = NimbusAdManagerTestListener(identifier = item) { controller ->
                    controller.listeners.add(LoggingAdControllerListener(identifier = item))
                    controller.align { Gravity.TOP or Gravity.CENTER_HORIZONTAL }
                },
            )
            "Interstitial Static" -> adManager.showBlockingAd(
                request = NimbusRequest.forInterstitialAd(item).apply {
                    request.imp[0].video = null
                },
                closeButtonDelaySeconds = 0,
                activity = requireActivity(),
                listener = NimbusAdManagerTestListener(identifier = item) { controller ->
                    controller.listeners.add(LoggingAdControllerListener(identifier = item))
                },
            )
            "Interstitial Video" -> adManager.showBlockingAd(
                request = NimbusRequest.forInterstitialAd(item).apply {
                    request.imp[0].banner = null
                },
                closeButtonDelaySeconds = 0,
                activity = requireActivity(),
                listener = NimbusAdManagerTestListener(identifier = item) { controller ->
                    controller.listeners.add(LoggingAdControllerListener(identifier = item))
                },
            )
            "Interstitial Hybrid" -> adManager.showBlockingAd(
                request = NimbusRequest.forInterstitialAd(item),
                activity = requireActivity(),
                listener = NimbusAdManagerTestListener(identifier = item) { controller ->
                    controller.listeners.add(LoggingAdControllerListener(identifier = item))
                },
            )
            "Rewarded Video (5 Sec)" -> adManager.showRewardedAd(
                request = NimbusRequest.forRewardedVideo(item),
                activity = requireActivity(),
                closeButtonDelaySeconds = 5,
                listener = NimbusAdManagerTestListener(identifier = item) { controller ->
                    controller.listeners.add(LoggingAdControllerListener(identifier = item))
                },
            )
            "Ads in ScrollView" -> {
                LayoutAdsInListBinding.inflate(inflater, adFrame, true).apply {
                    adManager.showAd(
                        request =  NimbusRequest.forBannerAd("$item Banner", Format.BANNER_320_50, Position.HEADER),
                        refreshInterval = 30,
                        viewGroup = adFrameBanner,
                        listener = NimbusAdManagerTestListener(identifier = item) { controller ->
                            controller.listeners.add(LoggingAdControllerListener(identifier = item))
                        },
                    )
                    adManager.showAd(
                        request =  NimbusRequest.forBannerAd("$item Inline Interstitial", Format.INTERSTITIAL_PORT),
                        viewGroup = adFrameImage,
                        listener = NimbusAdManagerTestListener(identifier = item) { controller ->
                            controller.listeners.add(LoggingAdControllerListener(identifier = item))
                        },
                    )
                    adManager.showAd(
                        request =  NimbusRequest.forVideoAd("$item Video"),
                        refreshInterval = 30,
                        viewGroup = adFrameVideo,
                        listener = NimbusAdManagerTestListener(identifier = item) { controller ->
                            controller.listeners.add(LoggingAdControllerListener(identifier = item))
                        },
                    )
                }
            }
        }
    }.root

    override fun onDestroyView() {
        RequestManager.interceptors.remove(this)
        adController?.destroy()
        adController = null
        super.onDestroyView()
    }

    override fun modifyRequest(request: NimbusRequest) {
        request.request.imp[0].ext.facebook_app_id = null
        request.request.user?.ext = request.request.user?.ext?.apply {
            facebook_buyeruid = null
            unity_buyeruid = null
            vungle_buyeruid = null
        }
    }
}
