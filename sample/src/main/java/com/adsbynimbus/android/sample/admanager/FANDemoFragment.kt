package com.adsbynimbus.android.sample.admanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.adsbynimbus.NimbusAd
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.databinding.FragmentFanDemoBinding
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.AdEvent
import com.adsbynimbus.render.FANAdRenderer
import com.adsbynimbus.render.Renderer
import timber.log.Timber

/**
 * This Fragment shows what Facebook ads look like when run through the Nimbus renderer but is not
 * indicative of normal usage as the Nimbus server determines which ad units to request based on the
 * request sent from the client.
 */
class FANDemoFragment : Fragment(), Renderer.Listener, AdController.Listener {

    private var adController: AdController? = null
    private val args: FANDemoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentFanDemoBinding.inflate(inflater, container, false).apply {
        headerView.setTitleText(args.titleText)
        headerView.setSubtitleText(args.subtitleText)

        val nimbusAd = object : NimbusAd {
            override fun placementId(): String =
                args.item.placementId(args.item.testAdTypes.random())

            override fun type(): String = args.item.adType

            override fun network(): String = FANAdRenderer.FACEBOOK

            override fun isInterstitial(): Boolean = args.item.isInterstitial

            override fun markup(): String = ""

            override fun width(): Int = if (args.item == FANAdItem.BANNER) 320 else 0

            override fun height(): Int = if (args.item == FANAdItem.BANNER) 50 else 0

            override fun trackersForEvent(event: AdEvent): Collection<String> = emptyList()
        }
        when (args.item) {
            FANAdItem.BANNER, FANAdItem.NATIVE ->
                Renderer.loadAd(nimbusAd, adLayout, this@FANDemoFragment)
            FANAdItem.INTERSTITIAL ->
                Renderer.loadBlockingAd(nimbusAd, requireActivity())?.apply {
                    listeners().add(this@FANDemoFragment)
                } ?: run { Timber.i("No placement id for that ad type") }
        }
    }.root

    override fun onDestroyView() {
        adController?.destroy()
        adController = null
        super.onDestroyView()
    }

    override fun onAdEvent(adEvent: AdEvent) {
        Timber.i("${args.item.description}: %s", adEvent.name)
    }

    override fun onAdRendered(controller: AdController) {
        adController = controller.also {
            it.listeners().add(this)
            it.start()
        }
    }

    override fun onError(error: NimbusError) {
        Timber.e("${args.item.description}: %s", error.message)
    }
}
