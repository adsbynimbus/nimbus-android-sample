package com.adsbynimbus.android.sample.rendering

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAd
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.databinding.LayoutAdsInListBinding
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.openrtb.enumerations.Position
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.AdEvent
import com.adsbynimbus.render.Renderer
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
                            Renderer.loadAd(object : NimbusAd {
                                override fun type() = "static"

                                override fun markup() = adMarkup4

                                override fun width() = 320

                                override fun height() = 250
                            }, adFrame,
                                object : Renderer.Listener, NimbusError.Listener {
                                    override fun onAdRendered(controller: AdController) {
                                        controller.volume = 100
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

val adMarkup2 = """
<html>
<head>
  <meta name="viewport" content="width=device-width,initial-scale=1.0,user-scalable=no">
  <style>html,body{overflow:hidden;margin:0;padding:0;height:100%;width:100%}</style>
</head>
<body>

    <script src="https://noti-us.adsmoloco.com/rtb/win?ctx=CgNVU0ESDQoJMzA1MzQzNDA0EAEYAiIQZVVlaEFRczN5Tm1sZkI5SCokMDIyMTgzMzctODJCRi00MjAxLUI0ODMtQkE3QkZFMTk4OTE0&exchange=NIMBUS&force_stream=false&mtid=ChDfAR3IP7lHCbnOJbrIYL7wEPqXlrMGGhQIBhoQO665woMDQ3eXPuxYIIcAAiABKgAyAA&price=0.6670"></script><script src="https://assets.lunalabs.io/cdn/d182baa80fc460b1fd5d85dccc90e568/data/js/trampoline.js" data-relative-src="js/trampoline.js"></script><script src="mraid.js"></script><script>window.MOLOCO_BIDTIMEMACROS={mraidViewable:unescape("https%3A%2F%2Fnoti-us.adsmoloco.com%2Frtb%2Fimptrace%3Fcr_id%3DBZVKTzzvKk1EtzFO%253Ai%253AVnuGJb3YqsFzKRpj%26ctx%3DCDgSA1VTQRoJMzA1MzQzNDA0KjEIAhIGMTcuNC4xGhJDT05ORUNUSU9OX1VOS05PV04qBWFwcGxlMgppUGhvbmUxMiw4MAI6EGVVZWhBUXMzeU5tbGZCOUhCDQoJMzA1MzQzNDA0EAE%26event_type%3Dmraid_viewable%26exchange%3DNIMBUS%26force_stream%3Dfalse%26is_v2%3Dtrue%26mtid%3DChDfAR3IP7lHCbnOJbrIYL7wEPqXlrMGGhQIBhoQO665woMDQ3eXPuxYIIcAAiABKgAyAA"),gameViewable:unescape("https%3A%2F%2Fnoti-us.adsmoloco.com%2Frtb%2Fimptrace%3Fcr_id%3DBZVKTzzvKk1EtzFO%253Ai%253AVnuGJb3YqsFzKRpj%26ctx%3DCDgSA1VTQRoJMzA1MzQzNDA0KjEIAhIGMTcuNC4xGhJDT05ORUNUSU9OX1VOS05PV04qBWFwcGxlMgppUGhvbmUxMiw4MAI6EGVVZWhBUXMzeU5tbGZCOUhCDQoJMzA1MzQzNDA0EAE%26event_type%3Dgame_viewable%26exchange%3DNIMBUS%26force_stream%3Dfalse%26is_v2%3Dtrue%26mtid%3DChDfAR3IP7lHCbnOJbrIYL7wEPqXlrMGGhQIBhoQO665woMDQ3eXPuxYIIcAAiABKgAyAA"),engagement:unescape("https%3A%2F%2Fnoti-us.adsmoloco.com%2Frtb%2Fimptrace%3Fcr_id%3DBZVKTzzvKk1EtzFO%253Ai%253AVnuGJb3YqsFzKRpj%26ctx%3DCDgSA1VTQRoJMzA1MzQzNDA0KjEIAhIGMTcuNC4xGhJDT05ORUNUSU9OX1VOS05PV04qBWFwcGxlMgppUGhvbmUxMiw4MAI6EGVVZWhBUXMzeU5tbGZCOUhCDQoJMzA1MzQzNDA0EAE%26event_type%3Dengagement%26exchange%3DNIMBUS%26force_stream%3Dfalse%26is_v2%3Dtrue%26mtid%3DChDfAR3IP7lHCbnOJbrIYL7wEPqXlrMGGhQIBhoQO665woMDQ3eXPuxYIIcAAiABKgAyAA"),complete:unescape("https%3A%2F%2Fnoti-us.adsmoloco.com%2Frtb%2Fimptrace%3Fcr_id%3DBZVKTzzvKk1EtzFO%253Ai%253AVnuGJb3YqsFzKRpj%26ctx%3DCDgSA1VTQRoJMzA1MzQzNDA0KjEIAhIGMTcuNC4xGhJDT05ORUNUSU9OX1VOS05PV04qBWFwcGxlMgppUGhvbmUxMiw4MAI6EGVVZWhBUXMzeU5tbGZCOUhCDQoJMzA1MzQzNDA0EAE%26event_type%3Dcomplete%26exchange%3DNIMBUS%26force_stream%3Dfalse%26is_v2%3Dtrue%26mtid%3DChDfAR3IP7lHCbnOJbrIYL7wEPqXlrMGGhQIBhoQO665woMDQ3eXPuxYIIcAAiABKgAyAA"),click:unescape("https%3A%2F%2Ftr-us.adsmoloco.com%2Frtb%2Fclick%3Fexchange%3DNIMBUS%26imp_id%3D3a9d02f7-e0ce-4c77-b483-c3a7419c253a%26info%3DChDfAR3IP7lHCbnOJbrIYL7wEPqXlrMGGhQIBhoQO665woMDQ3eXPuxYIIcAAiABKgAyAA%26campaign_name%3DeUehAQs3yNmlfB9H%26dcr%3D"),finalUrl:unescape("https%3A%2F%2Fapps.apple.com%2Fus%2Fapp%2Froyal-match%2Fid1482155847")}</script><img src="https://tr-us.adsmoloco.com/rtb/imp?exchange=NIMBUS&info=ChDfAR3IP7lHCbnOJbrIYL7wEPqXlrMGGhQIBhoQO665woMDQ3eXPuxYIIcAAiABKgAyAA&campaign_name=eUehAQs3yNmlfB9H&imp_id=3a9d02f7-e0ce-4c77-b483-c3a7419c253a&price=0.6670" height="1" width="1"><img src="https://tr-us.adsmoloco.com/rtb/imp_fwd?exchange=NIMBUS&info=ChDfAR3IP7lHCbnOJbrIYL7wEPqXlrMGGhQIBhoQO665woMDQ3eXPuxYIIcAAiABKgAyAA&price=0.6670" height="1" width="1"><script src="https://assets-1.lunalabs.io/cdn/d182baa80fc460b1fd5d85dccc90e568/data/assets/scripts.js" data-relative-src="assets/scripts.js"></script><script src="https://assets-3.lunalabs.io/cdn/d182baa80fc460b1fd5d85dccc90e568/data/assets/jsons.js" data-relative-src="assets/jsons.js"></script><script src="https://assets-2.lunalabs.io/cdn/d182baa80fc460b1fd5d85dccc90e568/data/assets/blobs.js" data-relative-src="assets/blobs.js"></script>
    <script src="https://turbo.adsbynimbus.com/impression_tracker/X0JNN2JwYl82Y01avhN3c83L2m1WzvugoxuO1SknmpRPm0Ch6zNHqEAeGrG5u2nbJxGPZphfGiqbQbnnD21grL9LFcriAwd0SP8Gqf4pwTM8gO03lj5PODbZr2GnTuBmjRaQa8wPamCj1qOmYOYkzopcW3nWWcf1hU5ICzB4jFrlZ_2KVpesT2xDogFoZOkoGYmuJRuzJci7mPzYA3oNm8tkWm1-RvNyBhfYs0d4cWgUxuxP90__ruhC24wR05sUlJRBFzX5z2gcJCbX8eAAPfdkWiypVdFrpLaflMZSjklVg8p9DV8oYV_ARgTeyx2h4HLBHvB1FfoqCE6pKoOPU7MolvKezPiP09Ev61zjRM1NtgC57ra9ZEJ_xGcBw_8Ql4qavzS6QIx011m4Psp1BItLj62T_hRU-HiT9lSfO9PV-_N11muIa3mpGcNj-PJ-UyGMUNS60NJ2rN6_ukSrQhp-saZybI28ul6CI4GDK9AZYLOC4KWpRhxDJRpB4I3-7Ar040N7aIFBr0NhiFAFFVRMdOhqYOvUheM05UbQ48V3MEQ6NDjJXyyb4TbGJ_Rz3iWW9baf7tpJ7T50rQB6A0d-OOxs7Rhi3bRLuEDM64j-lkSdFw5LN9OGuUSxRbfFO5qRjMd8-fLCvFamuxgaNPj3hw5WL4i7udMXuGZ-Sv8F1HY8pHJswuzS2tRiP-DJ8QBB_5M-UUqTrSZaEHcFvV_b-zAb1RegTmIFw6yEjkbGOquMgxc9h-ydURXNDH5ZgXzVW3y1uqLvk729XRh2MbmGbAY82ewyBYKaqmKFbf8mJk16OvcMNqvSOzwA3VmvWtRRbVjNIM63xziuEtschIWPgbMlHEO7fsIp2XESVgAm_cmWGlujETartq_JIgzCYyC1fO09umrVsJ_u_XA7RfUMzufUPQJ_-Lnq9xnny3A6Z1bYfiP6ZbTijpmAh1xj4s-dgFLfLddfzm-f44mczMKQfsv0OOXD6a5_KiTJ53DBMHlw_f9gpluBwsUYEeoc-CFZbRIGyPirLWUGK17bCwL-IaYQ4dike8pQMg0HBD-1Fak_WEkKyRtsDHYx-FTVP0Bo29DZeQloufuUUNJPnSN9Ma_LGj9IFJGv3qlRbTA0GLUz_DYdnTmkGEEe07GKG4dgpJEs5i3I2BhB7QmA90rfmfWJfTD_zjxBCpmA4FdaL3ynW-_kJGtIYheKpHx5W6o0n4BnGMjLlXyQ99S0zhXmiPVAmQi-bG3ldJGn6ZEI9DYMl6MdOxJuCSk3xjOTkvZb8NepL9RMm3ExI8wJNIrKrP3tnuB2T_07Y8X_Y5xT-hXRFm3WeNgwXLOYp5q0TqjYsnJyu8PZjUHX__WLtgTO45RVPPRObqXpGRbRYz6e5WB6eQ69PL4XH-65oy3examr-L9PG1ddIqw6mvrrBEa5Y91rZlp0wbtcR3Ju_vgJyUGy7H7eYF2T0M4bm8dyosUpcIjKx2KMWqbt7wvwFj9qUxXwKVMPl_kHzb8Kd0ZxlSFlZs9BmhT0t6-sV00Ly0E4EKe2sRX-JN08JnDARgitdOJFUp6OOPqfBHDVtpbGQ_rp0WQpyReiyf2nqpCJzB5D7q9gA74x66YA1tLOaH6PQPbgp52c_i3Ton4sttKJtJ8SdHGKkmurlmD2WmdlfGfY9sJUpagzjsyYceDs"></script>

</body>
</html>
""".trim()

val adMarkup = """
<html>
<head>
  <meta name="viewport" content="width=device-width,initial-scale=1.0,user-scalable=no">
  <style>html,body{overflow:hidden;margin:0;padding:0;height:100%;width:100%}</style>
</head>
<body>

    <div>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=no"/>
  <a id="exchange-ping-url" href="https://cdn.liftoff-creatives.io/tracking_pixel.gif" style="display:none"></a>
  <script>window.LIFTOFF_START_TS = Date.now()</script>
  <script src="https://cdn.liftoff-creatives.io/resources/loading_indicator-3870ce387152717b8bc0.js"></script>
  <link href="https://cdn.liftoff-creatives.io/resources/loading_indicator-0e7efccd49baff002216.css" rel="stylesheet"/>
  <script src="mraid.js"></script>
  <div id="tracking-tag-holder" style="display:none">
    <img id="liftoff-beacon-1" src="https://impression-east.liftoff.io/nimbus/impression?ad_group_id=194150&amp;channel_id=42&amp;creative_id=546243&amp;device_id_sha1=&amp;source_app_store_id=305343404&amp;bid_ts=1728807193065&amp;auction_id=c0c47538-15ce-4c66-8858-468c2412ed7d&amp;loid=Xp7D569Vh89s2vAxnpR0&amp;auction_price=1.0975&amp;origin=haggler-nimbus757"/>
  </div>
  <script src="https://cdn.liftoff-creatives.io/resources/polyfills-d2172b8b55b66d989c58.js"></script>
  <link href="https://cdn.liftoff-creatives.io/resources/liftoff-0032566ea33076318ed6.css" rel="stylesheet"/>
  <script src="https://cdn.liftoff-creatives.io/resources/liftoff-e69ca2ab080eef65374b.js"></script>
  <script>
    Liftoff.init(JSON.parse(decodeURIComponent('{"has_html_endscreen":false,"creative_policy":{"show_chinese_watermark":false,"show_ad_choices":false,"use_custom_close":false,"audio_policy":"on-interaction","remove_ad_wrapper":false,"russian_age_rating":null,"close_delay":5,"add_border":false,"animation_duration_limit":0,"block_strobing":false,"block_notifications":false},"cta_text":null,"products":%5B%5D,"enable_html_return":false,"bid_context":{"last_chance_endscreen_autoclick_behavior":null,"dest_app_id":5476,"normalized_language":"en","cdnURL":"https://cdn.liftoff-creatives.io","creative_id":546243,"width":768,"source_app_name":"Tumblr %E2%80%93 Fandom, Art, Chaos","is_reengagement":false,"is_interactive":true,"source_app_id":"305343404","last_chance_endscreen_interaction_autoclick_behavior":null,"is_interstitial":true,"pv_autoclick_behavior":{"type":"viewableDelay","count":0,"delay":5000},"viewclick_behavior":{"type":"viewableDelay","count":0,"delay":5000},"exchange":"nimbus","country":"USA","ab_tests":%5B"crserve-skan-pod-control","3d-ads-experiment"%5D,"platform":"ios","height":1024},"html_return_delay":"0","click_config":{"clickthrough_url":{"url":"https://apps.apple.com/app/id1621328561%3Fmt=8","type":2},"click_tracking_urls":%5B{"url":"https://click.liftoff.io/v1/campaign_click/_DvvdoVTAMsu77biu2xekFLQ3iO62Qs_9V5BSNch0rAxrve-VX8PITuoCmYy_2lxQnFDE79jsF5oxrFB_fBDrBecUpQfN2j7cmdTwXjmqkWR5sj4Oc8R2BIucUFa_iieubektFcdjsxW82QsXsg7DXIunrnzs_D-UAdT0xnEWQ0yDVL1YSl2R81mJerAfLl25Y6DWNhS8D91BQMIlB02aWfKrQdayckaOXilLDYxn8sPcuItzsA9pAvYQpdYZG1oj7g-9JsSh5WivPLyvPJZwdPkxNn9RlbqLTE0Uz5DRrOHWs8ixgs4jweCKyngxZLumlr4VjKhweuILOeSt5r3pE2WbVeN59DO6vQ6bqTELuxperjpnyzjJxWW8Yd3Xfn8TXZAhvaYoOG-4UouiHSB1W7hw_V-Y2sXa5_U71S7I_FtaAOeYdrjbX9ZBenwCrOawY85katf0Q4fITdcpvMgh5JFXww9zgsdmJqDU4HX0Sb-Uq9syDSaFtvAQdquwdjuY58isDGaUO0D6SMb6k5MoWwjnKu-LeGyuadWSWAzuSN4Rd7mysoLd7KkTg","type":1}%5D},"app_store_data":null,"is_tre":false,"enable_logging":false,"logging_endpoint":"https://adexp.liftoff.io","event_tracking_param":"915939CObsCxIkYzBjNDc1MzgtMTVjZS00YzY2LTg4NTgtNDY4YzI0MTJlZDdk6gEUWHA3RDU2OVZoODlzMnZBeG5wUjAYqJPBp6gyICoow6shMOQqOgkzMDUzNDM0MDRCGGNyc2VydmUtc2thbi1wb2QtY29udHJvbEIRM2QtYWRzLWV4cGVyaW1lbnRKCjk4Yjc1MDkyMzJQAloDVVNBYAFoAXIJdXMtZWFzdC0x4AEBgAEqkgECZW6YAQGhAQAAAAAAALA_qgEINzY4eDEwMjSyARFTb2NpYWwgTmV0d29ya2luZ7oBHVR1bWJsciDigJMgRmFuZG9tLCBBcnQsIENoYW9zwgEZaHRtbC0yN2EzMDJhOGFmYTBkNDQxMzc2N9IBBTY0ODA02gEINzY4eDEwMjSAAgE","app_icon":"https://cdn.liftoff-creatives.io/customers/137/creatives/5476-icon-250x250.png"}')), {
      image_originals: {},
      video_originals: {},
    },
      JSON.parse(decodeURIComponent('%5B%5D'))
    );
  </script>
  <script src="https://cdn.liftoff-creatives.io/resources/madlib-9b3b45077d3c2790a6e4.js"></script>
  <link href="https://cdn.liftoff-creatives.io/resources/tresensa-snippet-eb5083c70ee7438a3796.css" rel="stylesheet"/>
  <script src="https://cdn.liftoff-creatives.io/resources/tresensa-snippet-b9ab6f5fd71fcaca79e7.js"></script>
<script>
  Madlib.load(Liftoff.snippetEl, JSON.parse(decodeURIComponent('{"scenes":%5B{"id":"main","layoutID":404,"transition":null,"layoutContent":{"css":{"Foreground":{"pointer-events":"none"}},"containers":%5B{"id":"Background","children":%5B%5D},{"id":"Main Content","children":%5B%5D},{"id":"Foreground","children":%5B%5D},{"id":"CONTAINER - DO NOT USE","children":%5B%5D}%5D,"constraints":{"Background":{"top":0,"left":0,"right":0,"bottom":0},"Foreground":{"top":0,"left":0,"right":0,"bottom":0},"Main Content":{"top":0,"left":0,"right":0,"bottom":0},"CONTAINER - DO NOT USE":{"width":0,"height":0}},"clickTargets":%5B"CONTAINER - DO NOT USE"%5D,"coordination":%5B{"event":{"data":{},"name":"beforeLayout","type":"container","source":"CONTAINER - DO NOT USE","system":true},"actions":%5B{"data":{},"name":"hide","type":"container","target":"CONTAINER - DO NOT USE"}%5D}%5D,"componentParams":{"Main Content":{"params":{"":{"type":"message","value":null},"html":{"type":"text","value":"{%5Cn    %5C"required_assets%5C": %5B%5Cn        %5C"images/CBBackground_backgroundImage.jpg%5C",%5Cn        %5C"images/CBBackground_backgroundImage_2x.jpg%5C",%5Cn        %5C"video/portraitCountdown.mp4%5C",%5Cn        %5C"images/initial.png%5C"%5Cn    %5D,%5Cn    %5C"tge%5C": %5C"1.2%5C",%5Cn    %5C"dst%5C": %5C"B0177%5C",%5Cn    %5C"dstVersion%5C": %5C"1.0.0%5C",%5Cn    %5C"variantData%5C": {%5Cn        %5C"master_uuid%5C": %5C"1edf4f6ec04940CB%5C",%5Cn        %5C"is_tresensa%5C": true,%5Cn        %5C"tracker_id%5C": %5C"55e2cc92-70c9-4562-9850-cf815995231c%5C",%5Cn        %5C"variant_uuid%5C": %5C"012d1797-b73d-34b1-a59c-62887d9851cb%5C"%5Cn    },%5Cn    %5C"loadCbComponents%5C": false,%5Cn    %5C"creative%5C": %5C"1edf4f6ec04940CB%5C"%5Cn}"},"advancedSettings":{"type":"text","value":null},"hasAutoplayVideo":{"type":"boolean","value":null}},"component":"tresensa-snippet"}}}}%5D}')));
</script>

</div>

    <script src="https://turbo.adsbynimbus.com/impression_tracker/ZzNDQWhqUDJPS0thxmRGJfgWzYP1xkpsVs02Sklsd5RVDsylE5QTxqNLH0th8V0O77x_KS-b1CYotQqU3l3_ziYQ3Q_dYFn7OES8LEHNQ-_JpTuRvW_1Y1zl4LbuRBJ_66l6sun_b50IhDOfWIiDqFas9RROaG0kqEsK9m_u8rtEox2puE8DAnhwNKMDc2hw0b9EyOxN7o2bWq1Nfy45o11jiUWkqOd94Lw8SB8G683bsEbqOhRJeFHGMsJBOseuRRLEeT0jdy0K1FtYNX3SV6oA6P9tfDdiV3YdO0yZzhcyGA7yU2iWbCBbQEX4ztiBOtsrCoQRoISXIlr_i-fBOWuo5CU8STp936PRNPdsqvrSSFvlilMlo7wyKN_hhe04ZJWzlCrYGQPgGjTWSCqFWaeceT9npgKAmgKcQA7qrphY0EJ0xNZKpjUmJ_JGb383TkUncSMTET3_l5VnUtq04Sc8K0Y1gylWjg-P6HY6WW09hu5tRs89AnRHcXYXZ6MPiOf80vJ5zoq5v-4wq89dTYUSb1U2-pqcdS9Ukf1Yn6oqqBTKD53F-EXP3fuZDEB068Op6ynBW7ABKX_jrdrYxxTPEg-km5TqlhC0NtdlTkKnrEbcp6ypfSaHdZk5q-bLd8wRav9WcvN8g65XDlCzNbr8QQEXXArBAktyOurbO7Hjqsp_ReSmhx15gKG5kiFC-KVTbuyFwZBYSGSf7YSprPDe2LJoLni_GMKfjQk3C5m3K-L55B6ZH2S6Vh9CXRi_EGB48rtRoZH8LwBRarFaW2M37CR013ugtt3SITZ9f4Y1sJroV1djlYu9-RPAmVVBo1eho3nNAENp5HL-FgU4Q6Qki7RK0oOz6qRqHrNmaWnMDZhYqAYHPPpdOqi2bLt7i-RgVhq69oZKQ4OC1-QA_KrFqlJ55CfUYgIoSqysenOwYpINBq712CbHQffmOHtLnH4v3xqPkNsFd5oAy6NHk-7Zl5Ks0JZHfO_y94cFIXov2d78f4d9CzFBMROipsy5_O9ZjD-xSUFR9-Eh9r2NZZ9hTWOUsZOuXCYHvh5efu5HEcZ7-cDttgoaeNSi3V7UTooovJhv_RLk2SRt1C1tqGGYzFQz6dvY05CF-WoBiVfnqJQjIshxWE7n9Sz_DZs6YVCSqw4OvpnO02tAeCK47N5b_8jxT-UlN3oaCoOYolEZIQtU6OkMweBQx_Z3z42EfQMELYWyqogacb6zloBlCF0lULyGBw6AVFrjfAmmJ_w6HiIcl04gCS4eDwiov25p2PfLjqeGOED4vlcHCc6FOCF7xna2Wy9_ZEaVoEjQn66Yar_1KMxsNYyhLPNohp7YIjhOUe8h5-r9xDPe57AWPEQKSn4peVUCZ9OEvf8-GOquaXryrWw3jBMY13JPjbsCX1_ERUx9GdrPg_ahM2XEcFP-LTFlYPcFVaVwueJ6fXUFFrdxC7R90grgePaDf_cVXm0JurpNZumSKOT7u9msRzOrfpHPqC49pkwpVR9lRWSXoWlw90FIAo6Niw1bVHxvmEEiu2Wq5NBYSok5TZ8vkVKtsRVokQXYoG48kOOvyPqEuRZFSwW7yfVzbUiR7ZYSnm8skq7GCmM4iKmm4YOW5HXqcNW4_KHIFaDus2lsp00OK3klcJQcAZdAdSPgRRI5nj-YftzxyLa_ot3Y3EMDp2WR4_-tQa8y7zgYW0C--3bnObUviu6GxZwGZYathhw29NCn0dxxDHqvrEpTXWVy_RKw-PjOKzzHTcIVWVjrveLlC-S1lEiht-mqVg"></script>

</body>
</html>
""".trim()


val adMarkup3 = """
<html>
<head>
  <meta name="viewport" content="width=device-width,initial-scale=1.0,user-scalable=no">
  <style>html,body{overflow:hidden;margin:0;padding:0;height:100%;width:100%}</style>
</head>
<body>
    <script src='mraid.js'></script><script>document.createElement('IMG').src="https://tlx.3lift.com/s2s/notify?px=1&pr=$\{AUCTION_PRICE}&ts=1729098409&aid=6896909488148400481050&ec=7255_217307_T21662685&n=GuwEaHR0cHM6Ly91cy1lYXN0LTEuZXZlbnQucHJvZC5iaWRyLmlvL2xvZy9pbXAvdHJsP3NpZT1DaEVJcmJPV3dnY1FTQmpja0luRXNaT0pBeElQQ2dOa2MzQVFIUmp4Z1FFZ2hZVUVHZ04wY213Z3RCOG9BVG9vYldsa0xqWkRNRUU0UkVWQkxUaEJNVFl0TkRNelF5MDRNekE1TFVORlFqWXpNRVUwUXpneE1XQUJlTjJTQzRBQnl5V1NBUU4wY215b0FRRENBUURLQVJoa2MzQXROalF4WmpJMU16TmhObU5rWWpabU5tWTFObUxTQVRaQ1NVUmZVa1ZSVlVWVFZGOUZUbFpKVWs5T1RVVk9WRjlVV1ZCRk9rRlFVQzFFUlVGTVgwbEVYMHhKVTFRNlpITndMVFEzTXpMYUFRNHlNREkwTVRBeE5qQXdNREF3TU9JQk9nb09DQU1RQVJBRUVBTVFBaEFIRUFZU0JBZ0JFQUVTQkFnREVBRVNCQWdFRUFFU0JBZ0dFQUVTQkFnRkVBRVNCQWdDRUFFU0JBZ0hFQUhxQVNodGFXUXVOa013UVRoRVJVRXRPRUV4TmkwME16TkRMVGd6TURrdFEwVkNOak13UlRSRE9ERXgtZ0VWRFRwQld6OFNEdlVGQWdFQkFRQUFBQUFBQUFBQSZ3cD0zLjY0MSZmaWU9SUxRZlNoVUlvT2FHQkJEQWhEMUNCbUozZEd3eU9VZ0JVQUZxQ0JEdzlSSVlBQ0FBY0xvY2lnRVFDZ1VJdWtBUUFSRUFBQUFBQUFEd1BfSUJEQmdDSUlZaEtnVUlBaENuQXc9PSbyApgCCKnlv7gGEhY2ODk2OTA5NDg4MTQ4NDAwNDgxMDUwGAAgASjXODDboQ04qrsDQAFIAFADYAhogIAEcLmkCJAB3ZeqCpgBofuRCqAB7sysCqgBALgBsAnAAe8NyAG5HPAB0gH4AbocgALvDZECAAAAgAuT6T%2BZAvYoXI%2FC9dg%2FqAIAsAIBuAIFwQIAAAAAAADwP8gCA9gCA%2BgCqrsD%2BAKlN5ADAJgDAaADALgDlKYPyAMA0gMKZHNwLTE4MjYyMeADrbCbhQHpAwAAAAAAAAAA8AO6HPkDAAAAAAAAAACABAmJBPYoXI%2FC9dg%2FwATTAdAEANoEGDY4OTY5MDk0ODgxNDg0MDA0ODEwNTAgMeAEAPAEAPgEAIAFAPgCEYgDAZIDBGJ6d3iYAwGgA%2BLBEagDALoDDDE3NC42OC4xNzUuOcoDFgoQIOO2qmltxTsbDJENDfScJRDw52o%3D";document.createElement('IMG').src="https://eb2.3lift.com/pe?fid=8&tid=21662685&peid=0&aid=6896909488148400481050";window.tl_auction_response_441265={"ad":{"id":"dsp-182621","adomain":["dspolitical.com"],"bundle":["com.enflick.android.TextNow"],"cat":["IAB11"],"attr":[],"display":{"api":[7],"priv":"https:\/\/optout.aboutads.info\/","native":{"link":{"trkr":[]},"asset":[{"data":{"value":"Advertising Partner","type":1}},{"data":{"value":"Watch to learn more","type":2}},{"title":{"text":"Sponsored Video"}},{"video":{"adm":"<VAST version='3.0'><Ad id='182621'><Wrapper><Error><![CDATA[https:\/\/us-east-1.event.prod.bidr.io\/log\/vasterror?error_event=ChEIrbOWwgcQSBjckInEsZOJAxIPCgNkc3AQHRjxgQEghYUEGN2SCyDLJTIDdHJs&error_code=[ERRORCODE]]]><\/Error><AdSystem>Beeswax<\/AdSystem><VASTAdTagURI><![CDATA[https:\/\/ad.doubleclick.net\/ddm\/pfadx\/N51801.2534303BEESWAX\/B32537545.405061589;sz=0x0;ord=[timestamp];dc_lat=;dc_rdid=;tag_for_child_directed_treatment=;tfua=;dc_tdv=1;dcmt=text\/xml;dc_sdk_apis=[APIFRAMEWORKS];dc_omid_p=[OMIDPARTNER];gdpr=$\{GDPR};gdpr_consent=$\{GDPR_CONSENT_755};dc_mpos=[BREAKPOSITION];ltd=]]><\/VASTAdTagURI><Impression><![CDATA[https:\/\/media.bidr.io\/1X1.png]]><\/Impression><Creatives><Creative><Linear><TrackingEvents><Tracking event='start'><![CDATA[https:\/\/us-east-1.event.prod.bidr.io\/log\/act\/trl?ai=ChEIrbOWwgcQSBjckInEsZOJAxIPCgNkc3AQHRjxgQEghYUEGgN0cmwgASoobWlkLjZDMEE4REVBLThBMTYtNDMzQy04MzA5LUNFQjYzMEU0QzgxMUDdkgtIyyVSA3RybGAAejoKDggDEAEQBBADEAIQBxAGEgQIBBABEgQIAhABEgQIBxABEgQIBRABEgQIAxABEgQIARABEgQIBhAB&]]><\/Tracking><Tracking event='firstQuartile'><![CDATA[https:\/\/us-east-1.event.prod.bidr.io\/log\/act\/trl?ai=ChEIrbOWwgcQSBjckInEsZOJAxIPCgNkc3AQHRjxgQEghYUEGgN0cmwgAioobWlkLjZDMEE4REVBLThBMTYtNDMzQy04MzA5LUNFQjYzMEU0QzgxMUDdkgtIyyVSA3RybGAAejoKDggDEAEQBBADEAIQBxAGEgQIBBABEgQIAhABEgQIBxABEgQIBRABEgQIAxABEgQIARABEgQIBhAB&]]><\/Tracking><Tracking event='midpoint'><![CDATA[https:\/\/us-east-1.event.prod.bidr.io\/log\/act\/trl?ai=ChEIrbOWwgcQSBjckInEsZOJAxIPCgNkc3AQHRjxgQEghYUEGgN0cmwgAyoobWlkLjZDMEE4REVBLThBMTYtNDMzQy04MzA5LUNFQjYzMEU0QzgxMUDdkgtIyyVSA3RybGAAejoKDggDEAEQBBADEAIQBxAGEgQIBBABEgQIAhABEgQIBxABEgQIBRABEgQIAxABEgQIARABEgQIBhAB&]]><\/Tracking><Tracking event='thirdQuartile'><![CDATA[https:\/\/us-east-1.event.prod.bidr.io\/log\/act\/trl?ai=ChEIrbOWwgcQSBjckInEsZOJAxIPCgNkc3AQHRjxgQEghYUEGgN0cmwgBCoobWlkLjZDMEE4REVBLThBMTYtNDMzQy04MzA5LUNFQjYzMEU0QzgxMUDdkgtIyyVSA3RybGAAejoKDggDEAEQBBADEAIQBxAGEgQIBBABEgQIAhABEgQIBxABEgQIBRABEgQIAxABEgQIARABEgQIBhAB&]]><\/Tracking><Tracking event='complete'><![CDATA[https:\/\/us-east-1.event.prod.bidr.io\/log\/act\/trl?ai=ChEIrbOWwgcQSBjckInEsZOJAxIPCgNkc3AQHRjxgQEghYUEGgN0cmwgBSoobWlkLjZDMEE4REVBLThBMTYtNDMzQy04MzA5LUNFQjYzMEU0QzgxMUDdkgtIyyVSA3RybGAAejoKDggDEAEQBBADEAIQBxAGEgQIBBABEgQIAhABEgQIBxABEgQIBRABEgQIAxABEgQIARABEgQIBhAB&]]><\/Tracking><Tracking event='mute'><![CDATA[https:\/\/us-east-1.event.prod.bidr.io\/log\/act\/trl?ai=ChEIrbOWwgcQSBjckInEsZOJAxIPCgNkc3AQHRjxgQEghYUEGgN0cmwgByoobWlkLjZDMEE4REVBLThBMTYtNDMzQy04MzA5LUNFQjYzMEU0QzgxMUDdkgtIyyVSA3RybGAAejoKDggDEAEQBBADEAIQBxAGEgQIBBABEgQIAhABEgQIBxABEgQIBRABEgQIAxABEgQIARABEgQIBhAB&]]><\/Tracking><Tracking event='unmute'><![CDATA[https:\/\/us-east-1.event.prod.bidr.io\/log\/act\/trl?ai=ChEIrbOWwgcQSBjckInEsZOJAxIPCgNkc3AQHRjxgQEghYUEGgN0cmwgCCoobWlkLjZDMEE4REVBLThBMTYtNDMzQy04MzA5LUNFQjYzMEU0QzgxMUDdkgtIyyVSA3RybGAAejoKDggDEAEQBBADEAIQBxAGEgQIBBABEgQIAhABEgQIBxABEgQIBRABEgQIAxABEgQIARABEgQIBhAB&]]><\/Tracking><Tracking event='pause'><![CDATA[https:\/\/us-east-1.event.prod.bidr.io\/log\/act\/trl?ai=ChEIrbOWwgcQSBjckInEsZOJAxIPCgNkc3AQHRjxgQEghYUEGgN0cmwgCSoobWlkLjZDMEE4REVBLThBMTYtNDMzQy04MzA5LUNFQjYzMEU0QzgxMUDdkgtIyyVSA3RybGAAejoKDggDEAEQBBADEAIQBxAGEgQIBBABEgQIAhABEgQIBxABEgQIBRABEgQIAxABEgQIARABEgQIBhAB&]]><\/Tracking><Tracking event='resume'><![CDATA[https:\/\/us-east-1.event.prod.bidr.io\/log\/act\/trl?ai=ChEIrbOWwgcQSBjckInEsZOJAxIPCgNkc3AQHRjxgQEghYUEGgN0cmwgCioobWlkLjZDMEE4REVBLThBMTYtNDMzQy04MzA5LUNFQjYzMEU0QzgxMUDdkgtIyyVSA3RybGAAejoKDggDEAEQBBADEAIQBxAGEgQIBBABEgQIAhABEgQIBxABEgQIBRABEgQIAxABEgQIARABEgQIBhAB&]]><\/Tracking><Tracking event='fullscreen'><![CDATA[https:\/\/us-east-1.event.prod.bidr.io\/log\/act\/trl?ai=ChEIrbOWwgcQSBjckInEsZOJAxIPCgNkc3AQHRjxgQEghYUEGgN0cmwgCyoobWlkLjZDMEE4REVBLThBMTYtNDMzQy04MzA5LUNFQjYzMEU0QzgxMUDdkgtIyyVSA3RybGAAejoKDggDEAEQBBADEAIQBxAGEgQIBBABEgQIAhABEgQIBxABEgQIBRABEgQIAxABEgQIARABEgQIBhAB&]]><\/Tracking><Tracking event='close'><![CDATA[https:\/\/us-east-1.event.prod.bidr.io\/log\/act\/trl?ai=ChEIrbOWwgcQSBjckInEsZOJAxIPCgNkc3AQHRjxgQEghYUEGgN0cmwgDCoobWlkLjZDMEE4REVBLThBMTYtNDMzQy04MzA5LUNFQjYzMEU0QzgxMUDdkgtIyyVSA3RybGAAejoKDggDEAEQBBADEAIQBxAGEgQIBBABEgQIAhABEgQIBxABEgQIBRABEgQIAxABEgQIARABEgQIBhAB&]]><\/Tracking><\/TrackingEvents><VideoClicks><ClickTracking><![CDATA[https:\/\/us-east-1.event.prod.bidr.io\/log\/clk\/trl?ai=ChEIrbOWwgcQSBjckInEsZOJAxIPCgNkc3AQHRjxgQEghYUEGgN0cmwiKG1pZC42QzBBOERFQS04QTE2LTQzM0MtODMwOS1DRUI2MzBFNEM4MTE43ZILQMslSAFSA3RybGAAejoKDggDEAEQBBADEAIQBxAGEgQIBxABEgQIAhABEgQIBBABEgQIARABEgQIBhABEgQIAxABEgQIBRAB&audit_flag_wp=3.641]]><\/ClickTracking><\/VideoClicks><\/Linear><\/Creative><\/Creatives><Extensions><\/Extensions><\/Wrapper><\/Ad><\/VAST>","ext":{"playbackmethod":2,"tlVideo":{"isVpaid":false,"durationMs":30000,"isWrapped":true}}}},{"data":{"value":"Sponsored By","type":533}},{"data":{"value":"3","type":534}},{"data":{"value":"Learn more","type":12}}],"ext":{}},"event":[]},"ext":{"tlFormatId":8,"tlAdditionalData":{"pr":"$\{AUCTION_PRICE}","bc":"3.641","aid":"6896909488148400481050","bmid":"7255","biid":"7077","sid":"217307","did":"56746","tid":"21662685","clid":"21265825","brid":"135737","adid":"dsp-182621","crid":"279369773","ts":"1729098409","bcud":"3641","ss":"17"},"templateId":210,"billableEvent":3,"billablePixel":"https:\/\/tlx.3lift.com\/s2s\/notify?px=1&pr=$\{AUCTION_PRICE}&ts=1729098409&aid=6896909488148400481050&ec=7255_217307_T21662685&n=GuwEaHR0cHM6Ly91cy1lYXN0LTEuZXZlbnQucHJvZC5iaWRyLmlvL2xvZy9pbXAvdHJsP3NpZT1DaEVJcmJPV3dnY1FTQmpja0luRXNaT0pBeElQQ2dOa2MzQVFIUmp4Z1FFZ2hZVUVHZ04wY213Z3RCOG9BVG9vYldsa0xqWkRNRUU0UkVWQkxUaEJNVFl0TkRNelF5MDRNekE1TFVORlFqWXpNRVUwUXpneE1XQUJlTjJTQzRBQnl5V1NBUU4wY215b0FRRENBUURLQVJoa2MzQXROalF4WmpJMU16TmhObU5rWWpabU5tWTFObUxTQVRaQ1NVUmZVa1ZSVlVWVFZGOUZUbFpKVWs5T1RVVk9WRjlVV1ZCRk9rRlFVQzFFUlVGTVgwbEVYMHhKVTFRNlpITndMVFEzTXpMYUFRNHlNREkwTVRBeE5qQXdNREF3TU9JQk9nb09DQU1RQVJBRUVBTVFBaEFIRUFZU0JBZ0JFQUVTQkFnREVBRVNCQWdFRUFFU0JBZ0dFQUVTQkFnRkVBRVNCQWdDRUFFU0JBZ0hFQUhxQVNodGFXUXVOa013UVRoRVJVRXRPRUV4TmkwME16TkRMVGd6TURrdFEwVkNOak13UlRSRE9ERXgtZ0VWRFRwQld6OFNEdlVGQWdFQkFRQUFBQUFBQUFBQSZ3cD0zLjY0MSZmaWU9SUxRZlNoVUlvT2FHQkJEQWhEMUNCbUozZEd3eU9VZ0JVQUZxQ0JEdzlSSVlBQ0FBY0xvY2lnRVFDZ1VJdWtBUUFSRUFBQUFBQUFEd1BfSUJEQmdDSUlZaEtnVUlBaENuQXc9PSbyApgCCKnlv7gGEhY2ODk2OTA5NDg4MTQ4NDAwNDgxMDUwGAAgASjXODDboQ04qrsDQAFIAFADYAhogIAEcLmkCJAB3ZeqCpgBofuRCqAB7sysCqgBALgBsAnAAe8NyAG5HPAB0gH4AbocgALvDZECAAAAgAuT6T%2BZAvYoXI%2FC9dg%2FqAIAsAIBuAIFwQIAAAAAAADwP8gCA9gCA%2BgCqrsD%2BAKlN5ADAJgDAaADALgDlKYPyAMA0gMKZHNwLTE4MjYyMeADrbCbhQHpAwAAAAAAAAAA8AO6HPkDAAAAAAAAAACABAmJBPYoXI%2FC9dg%2FwATTAdAEANoEGDY4OTY5MDk0ODgxNDg0MDA0ODEwNTAgMeAEAPAEAPgEAIAFAPgCEYgDAZIDBGJ6d3iYAwGgA%2BLBEagDALoDDDE3NC42OC4xNzUuOcoDFgoQIOO2qmltxTsbDJENDfScJRDw52o%3D&b=1","renderOptionsBm":65536,"viewability":{"moat":0,"ias":1,"adelaide":0,"dv":0},"bannerWidth":300,"bannerHeight":250,"dealId":"56746","externalCreativeTypeId":3}}};</script><script src="https://ib.3lift.com/ttj?inv_code=TextNow_Android_Static_300x250&tid=210" data-auction-response-id="441265" data-ss-id="17"></script>
</body>
</html>
""".trim()

val adMarkup4 = """
<html>

<head>
  
  <meta name="viewport" content="width=device-width,initial-scale=1.0,user-scalable=no">
  <style>
    html,
    body {
      overflow: hidden;
      margin: 0;
      padding: 0;
      height: 100%;
      width: 100%
    }
  </style>
  <script
    type="text/javascript">             window.addEventListener('load', function () { var events = ['mouseover', 'mousemove', 'mouseout', 'mouseenter', 'mouseleave', 'mousedown', 'mouseup', 'focus', 'blur', 'click', 'MSPointerDown', 'MSPointerUp', 'MSPointerCancel', 'MSPointerMove', 'MSPointerOver', 'MSPointerOut', 'MSPointerEnter', 'MSPointerLeave', 'MSGotPointerCapture', 'MSLostPointerCapture', 'pointerdown', 'pointerup', 'pointercancel', 'pointermove', 'pointerover', 'pointerout', 'pointerenter', 'pointerleave', 'gotpointercapture', 'lostpointercapture', 'touchstart', 'touchmove', 'touchend', 'touchenter', 'touchleave', 'touchcancel']; var b = document.getElementById("liftoff-snippet"), report = function (e) { console.log("Event: " + e.type); }                  for (var i = 0; i < events.length; i++) { b.addEventListener(events[i], report, false); } }, false);          </script>
</head>

<body>
  <div style="position:absolute;top:0;bottom:0;left:0;right:0;">
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=no" />
    <link href="https://cdn.liftoff-creatives.io/resources/last_chance-c2fdfaf28422ab715e09.css" rel="stylesheet" /> <a
      id="exchange-ping-url" href="https://cdn.liftoff-creatives.io/tracking_pixel.gif" style="display:none"></a>
    <script>window.LIFTOFF_START_TS = Date.now()</script>
    <script src="https://cdn.liftoff-creatives.io/resources/polyfills-d2172b8b55b66d989c58.js"></script>
    <script src="mraid.js"></script>
    <div id="tracking-tag-holder" style="display:none"> <img id="liftoff-beacon-1"
        src="https://impression-east.liftoff.io/nimbus/impression?ad_group_id=182918&amp;channel_id=42&amp;creative_id=358082&amp;device_id_sha1=&amp;source_app_store_id=305343404&amp;bid_ts=1713725010517&amp;auction_id=7e69c95f-119c-4fab-8922-41773a6c3288&amp;loid=UdCip876DoLvc631iYj0&amp;auction_price=1.7250&amp;origin=haggler-nimbus191" />
    </div>
    <script src="https://cdn.liftoff-creatives.io/resources/outer_env-87c9cd84768330528834.js"></script>
    <div id="liftoff-snippet"></div>
    <script>     LiftoffOuterEnv.init({ inProd: true, containerEl: document.getElementById("liftoff-snippet"), envData: JSON.parse(decodeURIComponent('{"clickURLs":{"clickthroughURL":{"url":"https://apps.apple.com/app/id1621328561%3Fmt=8","type":2},"clickTrackingURLs":%5B{"url":"https://click.liftoff.io/v1/campaign_click/dPypaCM0Ml4h9Sg0jQ1E36V6b5V_PRbjiDm-fzzVIXnT1JEvLpz8wjY_Hb67arbJSF-rR1WJI-xbEoUAj4oBZ14tu4p6cWeILnzZ5SD5vIE-MBuKpLn6jgkQRqLDPSqTCcnDS-MYHrWR2Qo4K0n_Cy4JbpNU7vOLMrDmEWhFvdI09k4nK_jNoEnP_kTCFce4WsMs0imnuzhBgg6hHxFI55mZ9bEYzNIl7p7xCJZOevIlhDq6M-C0JtkmrrI8S602h8YGh78yKXWri25S7wiC6H_6vTQhxqkL_uWxFedlb2LoCkJ4vbX9c34WF5uhnAUdNLwuvKyFdZllV24xWOXDefmKLtuWEqdAY5r5SqdCgLlOAA_gwZJBpzha6hp621DD1Wkn-eJHrmR0FecHwBj9xcUJDHkQvW9KfvJLJlAOPyOPIDblSL1SyzMTE4TXfjvTuAiPtEhxsxNMdUmD4a582kCg50GMPGY3WD7oNU72Iu04z8TPAmjkur4rvukZNyDrs9phH3eApFkCQXNlXutH-jIb9N6RxUQyqhAmZMVohDBR2Abp0Ncy1qBBglI","type":1}%5D},"has_html_endscreen":false,"costModel":null,"pinpointURL":"https://apps.apple.com/app/id1621328561%3Fmt=8","isReengagement":false,"impression":{"auctionID":"UdCip876DoLvc631iYj0","isRewarded":false},"loggingConfig":{"enableHTMLReturn":false,"eventTrackingParam":"109b87CIaVCxIkN2U2OWM5NWYtMTE5Yy00ZmFiLTg5MjItNDE3NzNhNmMzMjg46gEUVWRDaXA4NzZEb0x2YzYzMWlZajAY0ODhj_AxICoowu0VMOQqOgkzMDUzNDM0MDRCGGNyc2VydmUtc2thbi1wb2QtY29udHJvbEIYdWdjLWluLXBsYXlhYmxlcy1jb250cm9sQhp0cmUtbWFkbGliLXRlc3QtZXhwZXJpbWVudEIhcXVhbGl0eS1lbmhhbmNlZC12aWRlby1leHBlcmltZW50Qg4zZC1hZHMtY29udHJvbEIeY3N0dWRpby1zcGxpdC10ZW1wbGF0ZS1jb250cm9sSgo5OGI3NTA5MjMyUANaA1VTQWABaAFyCXVzLWVhc3QtMeABAYABKpIBAmVumAEBoQEAAAAAAACwP6oBBzMyMHg1NjiyARFTb2NpYWwgTmV0d29ya2luZ7oBHVR1bWJsciDigJMgRmFuZG9tLCBBcnQsIENoYW9zwgEhY3NwLWNyZWF0aXZlLTZiZTdkZGJmZmQ2YWMzYjM4YmFk0gEA2gEHMzIweDQ4MA","htmlReturnDelay":"0","enableLogging":false,"loggingEndpoint":"https://adexp.liftoff.io"},"clickConfig":{"clickthroughURL":{"url":"https://apps.apple.com/app/id1621328561%3Fmt=8","type":2},"clickTrackingURLs":%5B{"url":"https://click.liftoff.io/v1/campaign_click/dPypaCM0Ml4h9Sg0jQ1E36V6b5V_PRbjiDm-fzzVIXnT1JEvLpz8wjY_Hb67arbJSF-rR1WJI-xbEoUAj4oBZ14tu4p6cWeILnzZ5SD5vIE-MBuKpLn6jgkQRqLDPSqTCcnDS-MYHrWR2Qo4K0n_Cy4JbpNU7vOLMrDmEWhFvdI09k4nK_jNoEnP_kTCFce4WsMs0imnuzhBgg6hHxFI55mZ9bEYzNIl7p7xCJZOevIlhDq6M-C0JtkmrrI8S602h8YGh78yKXWri25S7wiC6H_6vTQhxqkL_uWxFedlb2LoCkJ4vbX9c34WF5uhnAUdNLwuvKyFdZllV24xWOXDefmKLtuWEqdAY5r5SqdCgLlOAA_gwZJBpzha6hp621DD1Wkn-eJHrmR0FecHwBj9xcUJDHkQvW9KfvJLJlAOPyOPIDblSL1SyzMTE4TXfjvTuAiPtEhxsxNMdUmD4a582kCg50GMPGY3WD7oNU72Iu04z8TPAmjkur4rvukZNyDrs9phH3eApFkCQXNlXutH-jIb9N6RxUQyqhAmZMVohDBR2Abp0Ncy1qBBglI","type":1}%5D},"cta_text":null,"cdnURL":"https://cdn.liftoff-creatives.io","geo":{"latitude":28.6344,"longitude":-81.6221},"isInterstitial":true,"appStoreData":null,"creative":{"id":358082,"width":320,"height":480,"name":"MonopolyGoPickOne_3.11.0_Accelerate_vyof1781","params":%5B%5D},"abTests":%5B"crserve-skan-pod-control","ugc-in-playables-control","tre-madlib-test-experiment","quality-enhanced-video-experiment","3d-ads-control","cstudio-split-template-control"%5D,"lastChanceAutoclickBehavior":null,"app":{"id":"305343404","name":"Tumblr %E2%80%93 Fandom, Art, Chaos"},"campaign":{"id":29162,"name":"monopoly-Liftoff-iOS-US-roas-20230412","params":%5B%5D},"viewclickBehavior":{"type":"viewableDelay","count":0,"delay":5000},"pvAutoclickBehavior":{"type":"viewableDelay","count":0,"delay":5000},"creativePolicy":{"animationDurationLimit":0,"audioPolicy":"on-interaction","russianAgeRating":null,"removeAdWrapper":false,"showAdChoices":false,"addBorder":false,"useCustomClose":false,"closeDelay":5,"blockNotifications":false,"showChineseWatermark":false,"blockStrobing":false},"lastChanceEndscreenAutoclickBehavior":null,"device":{"googleAID":null,"connectionType":3,"googleAIDSHA1":null,"ip":"174.211.226.235","idfaSHA1":"","androidIDMD5":null,"idfa":"","normalizedLanguage":"en","osVersion":"17.4.1","idfaMD5":null,"androidIDSHA1":null,"androidID":null,"platform":"ios","model":"iPhone 14"},"trackingToken":"v.2_g.182918_a.7e69c95f-119c-4fab-8922-41773a6c3288_c.42_t.ua_u.x","lastChanceInteractionAutoclickBehavior":null,"exchange":"nimbus","isStaffDevice":false,"platform":"ios"}')), scriptSrcURLs: ["https://cdn.liftoff-creatives.io/resources/polyfills-d2172b8b55b66d989c58.js", "https://cdn.liftoff-creatives.io/resources/inner_env-fa9ae7fe6a9869408003.js"], rootURL: "https://cdn.liftoff-creatives.io/customers/137/creatives/358082/", html: '%3Cscript src="https://cdn.liftoff.io/api/v1/adcreative/liftoff_api.js"%3E%3C/script%3E%3Cdiv id="tre_container"%3E%3C/div%3E%3Cscript src="js/gameconfig.js"%3E%3C/script%3E%3Cscript src="js/assets.js"%3E%3C/script%3E%3Cscript src="js/tge.js"%3E%3C/script%3E%3Cscript src="js/game.js"%3E%3C/script%3E%3Cscript src="js/placement.js"%3E%3C/script%3E%3Cscript src="js/adcontainer.js"%3E%3C/script%3E', externalHtmlURL: null, onViewableTrackingTags: JSON.parse(decodeURIComponent('%5B%5D')), });   </script>
  </div>
</body>

</html>    
"""
