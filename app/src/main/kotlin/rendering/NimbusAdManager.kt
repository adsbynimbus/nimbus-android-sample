package com.adsbynimbus.android.sample.rendering

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.databinding.LayoutAdsInListBinding
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.*
import com.adsbynimbus.request.*
import timber.log.Timber

class AdManagerFragment : Fragment(), NimbusRequest.Interceptor {

    val adManager: NimbusAdManager = NimbusAdManager()
    val controllers = mutableListOf<AdController>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        RequestManager.interceptors.add(this@AdManagerFragment)
        when (val item = requireArguments().getString("item")) {
            "Manually Rendered Ad" -> {
                adManager.makeRequest(
                    context = root.context,
                    request = NimbusRequest.forBannerAd(item, Format.BANNER_320_50, Position.HEADER),
                    listener = object : RequestManager.Listener {
                        override fun onAdResponse(nimbusResponse: NimbusResponse) {
                            // Render ad with response
                            Renderer.loadAd(nimbusResponse, adFrame,
                                object : Renderer.Listener, NimbusError.Listener {
                                    override fun onAdRendered(controller: AdController) {
                                        controllers.add(controller.apply {
                                            setTestDescription(response = nimbusResponse)
                                            align { Gravity.TOP or Gravity.CENTER_HORIZONTAL }
                                            /* Replace the following with your own AdController.Listener implementation */
                                            listeners.add(EmptyAdControllerListenerImplementation)
                                            listeners.add(OnScreenLogger(LogAdapter().also { logs.useAsLogger(it) }, nimbusResponse))
                                        })
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
            "Banner" -> {
                adManager.showAd(
                    request = NimbusRequest.forBannerAd(item, Format.BANNER_320_50, Position.HEADER),
                    viewGroup = adFrame,
                    listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                        controllers.add(controller.apply {
                            align { Gravity.TOP or Gravity.CENTER_HORIZONTAL }
                            /* Replace the following with your own AdController.Listener implementation */
                            listeners.add(EmptyAdControllerListenerImplementation)
                        })
                    },
                )
            }
            "Banner With Refresh" -> {
                adManager.showAd(
                    request = NimbusRequest.forBannerAd(
                        item,
                        Format.BANNER_320_50,
                        Position.HEADER
                    ),
                    refreshInterval = 30,
                    viewGroup = adFrame,
                    listener = NimbusAdManagerTestListener(
                        identifier = item,
                        logView = logs
                    ) { controller ->
                        controllers.add(controller.apply {
                            align { Gravity.TOP or Gravity.CENTER_HORIZONTAL }
                            /* Replace the following with your own AdController.Listener implementation */
                            listeners.add(EmptyAdControllerListenerImplementation)
                        })
                    }
                )
            }
            "Video With Refresh" -> {
                adManager.showAd(
                    request = NimbusRequest.forVideoAd(item),
                    refreshInterval = 30,
                    viewGroup = adFrame,
                    listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                        controllers.add(controller.apply {
                            align { Gravity.TOP or Gravity.CENTER_HORIZONTAL }
                            /* Replace the following with your own AdController.Listener implementation */
                            listeners.add(EmptyAdControllerListenerImplementation)
                        })
                    }
                )
            }
            "Inline Video" -> {
                adManager.showAd(
                    request = NimbusRequest.forVideoAd(item),
                    viewGroup = adFrame,
                    listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                        controllers.add(controller.apply {
                            align { Gravity.TOP or Gravity.CENTER_HORIZONTAL }
                            /* Replace the following with your own AdController.Listener implementation */
                            listeners.add(EmptyAdControllerListenerImplementation)
                        })
                    },
                )
            }
            "Interstitial Hybrid" -> {
                adManager.showBlockingAd(
                    request = NimbusRequest.forInterstitialAd(item),
                    activity = requireActivity(),
                    listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                        /* Replace the following with your own AdController.Listener implementation */
                        controller.listeners.add(EmptyAdControllerListenerImplementation)
                    },
                )
            }
            "Interstitial Static" -> {
                adManager.showBlockingAd(
                    request = NimbusRequest.forInterstitialAd(item).apply {
                        request.imp[0].video = null
                    },
                    closeButtonDelaySeconds = 0,
                    activity = requireActivity(),
                    listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                        /* Replace the following with your own AdController.Listener implementation */
                        controller.listeners.add(EmptyAdControllerListenerImplementation)
                    },
                )
            }
            "Interstitial Video" -> {
                adManager.showBlockingAd(
                    request = NimbusRequest.forInterstitialAd(item).apply {
                        request.imp[0].banner = null
                    },
                    closeButtonDelaySeconds = 0,
                    activity = requireActivity(),
                    listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                        /* Replace the following with your own AdController.Listener implementation */
                        controller.listeners.add(EmptyAdControllerListenerImplementation)
                    },
                )
            }
            "Interstitial Video Without UI" -> {
                adManager.showBlockingAd(
                    request = NimbusRequest.forInterstitialAd(item).apply {
                        request.imp[0].banner = null
                    },
                    closeButtonDelaySeconds = 0,
                    activity = requireActivity(),
                    listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                        controller.listeners.add(object : AdController.Listener {
                            override fun onAdEvent(adEvent: AdEvent) {
                                if (adEvent == AdEvent.LOADED) controller.view?.alpha = 0f
                            }

                            override fun onError(error: NimbusError) {}
                        })
                    },
                )
            }
            "Rewarded Video" -> {
                adManager.showRewardedAd(
                    request = NimbusRequest.forRewardedVideo(item),
                    activity = requireActivity(),
                    closeButtonDelaySeconds = 60,
                    listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                        /* Replace the following with your own AdController.Listener implementation */
                        controller.listeners.add(EmptyAdControllerListenerImplementation)
                    },
                )
            }
            "Ads in ScrollView" -> {
                LayoutAdsInListBinding.inflate(inflater, adFrame, true).apply {
                    adManager.showAd(
                        request =  NimbusRequest.forBannerAd("$item Banner", Format.BANNER_320_50, Position.HEADER),
                        refreshInterval = 30,
                        viewGroup = adFrameBanner,
                        listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                            /* Replace the following with your own AdController.Listener implementation */
                            controller.listeners.add(EmptyAdControllerListenerImplementation)
                            controllers.add(controller)
                        },
                    )
                    adManager.showAd(
                        request =  NimbusRequest.forBannerAd("$item Inline Interstitial", Format.INTERSTITIAL_PORT),
                        viewGroup = adFrameImage,
                        listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                            /* Replace the following with your own AdController.Listener implementation */
                            controller.listeners.add(EmptyAdControllerListenerImplementation)
                            controllers.add(controller)
                        },
                    )
                    adManager.showAd(
                        request =  NimbusRequest.forVideoAd("$item Video"),
                        refreshInterval = 30,
                        viewGroup = adFrameVideo,
                        listener = NimbusAdManagerTestListener(identifier = item, logView = logs) { controller ->
                            /* Replace the following with your own AdController.Listener implementation */
                            controller.listeners.add(EmptyAdControllerListenerImplementation)
                            controllers.add(controller)
                        },
                    )
                }
            }
        }
    }.root

    override fun onDestroyView() {
        super.onDestroyView()
        RequestManager.interceptors.remove(this)
        controllers.forEach { it.destroy() }
    }

    override fun modifyRequest(request: NimbusRequest) {
        request.request.imp[0].ext.facebook_app_id = null
        request.request.user?.ext = request.request.user?.ext?.apply {
            facebook_buyeruid = null
            unity_buyeruid = null
            mfx_buyerdata = null
            vungle_buyeruid = null
        }
    }
}
