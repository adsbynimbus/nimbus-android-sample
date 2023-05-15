package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.adManager
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.AdEvent
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.RequestManager
import timber.log.Timber

class UnityDemoFragment : Fragment(), NimbusRequest.Interceptor {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        RequestManager.interceptors.add(this@UnityDemoFragment)
        val item = requireArguments().run {
            headerTitle.text = getString("titleText")
            headerSubtitle.text = getString("subtitleText")
            getString("item")
        }
        when (item) {
            "Rewarded Video Unity" -> adManager.showRewardedAd(
                request = NimbusRequest.forRewardedVideo("Rewarded_Android"),
                closeButtonDelaySeconds = 30,
                activity = requireActivity(),
            ) { controller -> controller.addListener("Rewarded Video Controller (Unity)") }
        }
    }.root

    override fun onDestroyView() {
        RequestManager.interceptors.remove(this)
        super.onDestroyView()
    }

    override fun modifyRequest(request: NimbusRequest) {
        request.request.imp[0].ext.facebook_app_id = ""
    }

    private fun AdController.addListener(controllerName: String) {
        listeners.add(object : AdController.Listener {
            override fun onAdEvent(adEvent: AdEvent) {
                Timber.i("$controllerName: %s", adEvent.name)
            }

            override fun onError(error: NimbusError) {
                Timber.e("$controllerName: %s", error.message)
            }
        })
    }
}
