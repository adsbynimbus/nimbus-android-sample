package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.adManager
import com.adsbynimbus.android.sample.databinding.FragmentAdManagerBinding
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.AdEvent
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.RequestManager
import timber.log.Timber

class UnityDemoFragment : Fragment(), NimbusRequest.Interceptor {

    private var adController: AdController? = null
    lateinit var item: UnityAdItem

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentAdManagerBinding.inflate(inflater, container, false).apply {
        RequestManager.interceptors.add(this@UnityDemoFragment)
        val bundle = requireArguments()
        headerView.setTitleText(bundle.getString("titleText", ""))
        headerView.setSubtitleText(bundle.getString("subtitleText", ""))
        item = bundle.getSerializable("item") as UnityAdItem
        when (item) {
            UnityAdItem.REWARDED_VIDEO_UNITY -> adManager.showRewardedAd(
                NimbusRequest.forRewardedVideo("Rewarded_Android"),
                5,
                requireActivity(),
            ) {
                it.view?.id = R.id.nimbus_ad_view
                it.addListener("Rewarded Video Controller (Unity)")
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
    }

    private fun AdController.addListener(controllerName: String) {
        listeners.add(object : AdController.Listener {
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
