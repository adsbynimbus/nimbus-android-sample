package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.admanager.VungleAdItem
import com.adsbynimbus.android.sample.databinding.FragmentVungleDemoBinding
import com.adsbynimbus.android.sample.enabled
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.AdEvent
import com.adsbynimbus.render.Renderer
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.RequestManager
import com.adsbynimbus.request.VungleDemandProvider
import timber.log.Timber

class VungleDemoFragment : Fragment(), AdController.Listener, Renderer.Listener, NimbusAdManager.Listener {

    var adController: AdController? = null
    private lateinit var item : VungleAdItem

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentVungleDemoBinding.inflate(inflater, container, false).apply {
        VungleDemandProvider.enabled = true
        item = requireArguments().getSerializable("item") as VungleAdItem
        when (item) {
            VungleAdItem.BANNER ->
                NimbusAdManager()
                    .showAd(
                        NimbusRequest.forBannerAd(item.placementId, Format.BANNER_320_50),
                        adFrame,
                        this@VungleDemoFragment
                    )
            VungleAdItem.MREC ->
                NimbusAdManager()
                    .showAd(
                        NimbusRequest.forBannerAd(item.placementId, Format.MREC),
                        adFrame,
                        this@VungleDemoFragment
                    )
            VungleAdItem.INTERSTITIAL ->
                NimbusAdManager()
                    .showBlockingAd(
                        NimbusRequest.forInterstitialAd(item.placementId),
                        requireActivity(),
                        this@VungleDemoFragment
                    )
            VungleAdItem.REWARDED ->
                NimbusAdManager()
                    .showBlockingAd(
                        NimbusRequest.forRewardedVideo(item.placementId),
                        requireActivity(),
                        this@VungleDemoFragment
                    )
        }

        headerView.setTitleText(requireArguments().getString("titleText", ""))
        headerView.setSubtitleText(requireArguments().getString("subtitleText", ""))
    }.root

    override fun onDestroyView() {
        VungleDemandProvider.enabled = false
        adController?.destroy()
        adController = null
        super.onDestroyView()
    }

    override fun onAdEvent(adEvent: AdEvent) {
        Timber.i("${item.description}: %s", adEvent.name)
    }

    override fun onAdRendered(controller: AdController) {
        Timber.i("${item.description}: onAdRendered")
        adController = controller.also {
            it.listeners().add(this)
            it.start()
        }
    }

    override fun onError(error: NimbusError) {
        Timber.e(error, "${item.description}: %s", error.message)
    }
}
