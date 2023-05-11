package com.adsbynimbus.android.sample.test

import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.NimbusError
import com.adsbynimbus.render.R
import com.adsbynimbus.android.sample.R.id.nimbus_ad_view
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.AdEvent
import com.adsbynimbus.request.NimbusResponse
import timber.log.Timber

class NimbusAdManagerTestListener(
    val identifier: String,
    val onAdRenderedCallback: (AdController) -> Unit,
) : NimbusAdManager.Listener {

    lateinit var response: NimbusResponse
    override fun onError(error: NimbusError) {
        Timber.e("$identifier: ${error.message}")
    }

    override fun onAdResponse(nimbusResponse: NimbusResponse) {
        response = nimbusResponse
    }

    override fun onAdRendered(controller: AdController) {

        controller.listeners.add(object : AdController.Listener {
            override fun onAdEvent(adEvent: AdEvent) {
                if (adEvent == AdEvent.LOADED) controller.view?.apply {
                    val testIdentifier = "${response.network()} ${response.type()} ad"
                    if (id != R.id.nimbus_refreshing_controller) id = nimbus_ad_view
                    contentDescription = testIdentifier
                }
            }
            override fun onError(error: NimbusError) {}
        })
        onAdRenderedCallback(controller)
    }
}

class LoggingAdControllerListener(val identifier: String, ) : AdController.Listener {
    override fun onAdEvent(adEvent: AdEvent) {
        Timber.i("$identifier: ${adEvent.name}")
    }

    override fun onError(error: NimbusError) {
        Timber.e("$identifier: ${error.message}")
    }
}
