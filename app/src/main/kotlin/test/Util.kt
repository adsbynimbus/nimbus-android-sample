package com.adsbynimbus.android.sample.test

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.adsbynimbus.NimbusAd
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.NimbusError
import com.adsbynimbus.render.R
import com.adsbynimbus.android.sample.R.id.nimbus_ad_view
import com.adsbynimbus.android.sample.R.string.custom_dialog_message
import com.adsbynimbus.android.sample.databinding.CustomDialogBinding
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.AdEvent
import com.adsbynimbus.render.Interceptor
import com.adsbynimbus.request.NimbusResponse
import timber.log.Timber

/** A Debug description of the Nimbus Response used for UI testing */
inline val NimbusAd.testDescription
    get() = "${network()} ${type()}" + if (width() != 0 && height() != 0) " ${width()}x${height()}" else ""

/** Sets debug information on the AdController for use with UI testing */
fun AdController.setTestDescription(response: NimbusAd?) {
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
        controller.listeners.add(LoggingAdControllerListener(identifier))
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

/** Listener that will append events and errors to a TextView */
class OnScreenAdControllerLogger(val view: TextView) : AdController.Listener {
    override fun onAdEvent(adEvent: AdEvent) {
        view.text = buildString {
            appendLine(view.text)
            appendLine("Event: ${adEvent.name}")
        }
    }

    override fun onError(error: NimbusError) {
        view.text = buildString {
            appendLine(view.text)
            appendLine("Error: ${error.errorType.name}")
            appendLine(error.message)
        }
    }
}

fun Context.showPropertyMissingDialog(property: String) {
    AlertDialog.Builder(this).setCancelable(false).create().apply {
        setView(CustomDialogBinding.inflate(LayoutInflater.from(context)).apply {
            description.text = getString(custom_dialog_message, property)
            button.setOnClickListener { dismiss() }
        }.root)
    }.show()
}

object UiTestInterceptor : Interceptor {
    override fun modifyAd(ad: NimbusAd): NimbusAd = ad

    override fun modifyController(ad: NimbusAd, controller: AdController): AdController = controller.apply {
        controller.setTestDescription(ad)
    }
}
