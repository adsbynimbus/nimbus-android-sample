package com.adsbynimbus.android.sample.test

import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.NimbusError
import com.adsbynimbus.render.R
import com.adsbynimbus.android.sample.R.id.nimbus_ad_view
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.AdEvent
import com.adsbynimbus.request.NimbusResponse
import timber.log.Timber

/** A Debug description of the Nimbus Response used for UI testing */
inline val NimbusResponse.testDescription get() = "${network()} ${type()} ad"

/** Sets debug information on the AdController for use with UI testing */
fun AdController.setTestDescription(response: NimbusResponse?) {
    view?.apply {
        if (id != R.id.nimbus_refreshing_controller) id = nimbus_ad_view
        contentDescription = response?.testDescription
    }
}

/**
 * An example implementation of the [NimbusAdManager.Listener] interface used throughout the sample app.
 *
 * This class is responsible for printing request and rendering errors to the console as well as setting the
 * [testDescription] property on the views managed by the Nimbus [AdController] interface.
 */
class NimbusAdManagerTestListener(
    val identifier: String,
    val onAdRenderedCallback: (AdController) -> Unit,
) : NimbusAdManager.Listener {

    var response: NimbusResponse? = null
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

                }
            }
            override fun onError(error: NimbusError) {}
        })
        onAdRenderedCallback(controller)
    }
}

/**
 * An example implementation of the [AdController.Listener] interface which logs events to the console.
 *
 * @param identifier used for prefixing logs to differentiate between multiple ads on screen at the same time.
 */
class LoggingAdControllerListener(val identifier: String) : AdController.Listener {
    override fun onAdEvent(adEvent: AdEvent) {
        Timber.i("$identifier: ${adEvent.name}")
    }

    override fun onError(error: NimbusError) {
        Timber.e("$identifier: ${error.message}")
    }
}
