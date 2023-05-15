package com.adsbynimbus.android.sample.admanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAd
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.AdEvent
import com.adsbynimbus.render.FANAdRenderer
import com.adsbynimbus.render.Renderer
import com.adsbynimbus.render.Renderer.Companion.loadBlockingAd
import timber.log.Timber

/**
 * This Fragment shows what Facebook ads look like when run through the Nimbus renderer but is not
 * indicative of normal usage as the Nimbus server determines which ad units to request based on the
 * request sent from the client.
 */
class FANDemoFragment : Fragment(), Renderer.Listener, AdController.Listener {

    private var adController: AdController? = null
        set(controller) { field = controller?.apply { listeners.add(this@FANDemoFragment) } }

    lateinit var item: FANAdItem

    private val FANAdItem.nimbusAd get() = object : NimbusAd {
        override fun placementId(): String = placementId(testAdTypes.random())

        override fun type(): String = adType

        override fun network(): String = FANAdRenderer.FACEBOOK

        override fun isInterstitial(): Boolean = isInterstitial

        override fun markup(): String = ""

        override fun width(): Int = width

        override fun height(): Int = height

        override fun trackersForEvent(event: AdEvent): Collection<String> = emptyList()
    }

    private fun ViewGroup.loadAd(item: FANAdItem) = when (item) {
        FANAdItem.BANNER,
        FANAdItem.NATIVE -> Renderer.loadAd(item.nimbusAd, this, this@FANDemoFragment)
        FANAdItem.INTERSTITIAL -> {
            adController = context.loadBlockingAd(item.nimbusAd).also {
                if (it == null) Timber.i("No placement id for that ad type")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutInlineAdBinding.inflate(inflater, container, false).apply {
        item = requireArguments().run {
            headerTitle.text = getString("titleText", "")
            headerSubtitle.text = getString("subtitleText", "")
            getSerializable("item") as FANAdItem
        }
        adFrame.loadAd(item)
    }.root

    override fun onDestroyView() {
        adController?.destroy()
        adController = null
        super.onDestroyView()
    }

    override fun onAdEvent(adEvent: AdEvent) {
        Timber.i("${item.description}: %s", adEvent.name)
    }

    override fun onAdRendered(controller: AdController) {
        adController = controller.also {
            it.listeners().add(this)
            it.start()
        }
    }

    override fun onError(error: NimbusError) {
        Timber.e("${item.description}: %s", error.message)
    }
}
