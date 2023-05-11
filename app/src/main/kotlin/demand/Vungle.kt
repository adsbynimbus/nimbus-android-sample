package com.adsbynimbus.android.sample.demand

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.adManager
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.enabled
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.AdEvent
import com.adsbynimbus.render.Renderer
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.VungleDemandProvider
import timber.log.Timber

fun initializeVungle() {
}
/**
 *
 */
fun ViewGroup.showBannerAd() {

}

class VungleFragment : Fragment(), AdController.Listener, Renderer.Listener, NimbusAdManager.Listener {

    var adController: AdController? = null
    lateinit var item: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        VungleDemandProvider.enabled = true
        logContainer.visibility = View.VISIBLE
        when (requireArguments().getString("item", "").also { item = it }) {
            "Vungle Banner" -> adManager.showAd(
                request = NimbusRequest.forBannerAd(item, Format.BANNER_320_50),
                viewGroup = adFrame,
                listener = this@VungleFragment,
            )
            "Vungle MREC" -> adManager.showAd(
                request = NimbusRequest.forBannerAd(item, Format.MREC),
                viewGroup = adFrame,
                listener = this@VungleFragment,
            )
            "Vungle Interstitial" -> adManager.showBlockingAd(
                request =  NimbusRequest.forInterstitialAd(item),
                activity = requireActivity(),
                listener = this@VungleFragment,
            )
            "Vungle Rewarded" -> adManager.showBlockingAd(
                request = NimbusRequest.forRewardedVideo(item),
                activity = requireActivity(),
                listener = this@VungleFragment
            )
        }
    }.root

    override fun onDestroyView() {
        VungleDemandProvider.enabled = false
        adController?.destroy()
        adController = null
        super.onDestroyView()
    }

    override fun onAdEvent(adEvent: AdEvent) {
        Timber.i("$item: %s", adEvent.name)
    }

    override fun onAdRendered(controller: AdController) {
        Timber.i("$item: onAdRendered")
        adController = controller.also {
            it.listeners().add(this)
            it.start()
        }
    }

    override fun onError(error: NimbusError) {
        Timber.e(error, "$item: %s", error.message)
    }
}
