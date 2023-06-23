package com.adsbynimbus.android.sample.rendering

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adsbynimbus.NimbusAd
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.NimbusError
import com.adsbynimbus.render.R
import com.adsbynimbus.android.sample.R.id.nimbus_ad_view
import com.adsbynimbus.android.sample.R.string.custom_dialog_message
import com.adsbynimbus.android.sample.TextViewHolder
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
    val logView: RecyclerView,
    var response: NimbusResponse? = null,
    val onAdRenderedCallback: (AdController) -> Unit,
) : NimbusAdManager.Listener {
    val adapter = logView.adapter as? LogAdapter ?: LogAdapter().also { logView.useAsLogger(it) }
    var onScreenLogger: OnScreenLogger? = null

    override fun onError(error: NimbusError) {
        adapter.submitList(buildList {
            addAll(adapter.currentList)
            add("Error: ${error.errorType.name}" + error.message?.let { " - $it" })
        })
        Timber.e("$identifier: ${error.message}")
    }

    override fun onAdResponse(nimbusResponse: NimbusResponse) {
        response = nimbusResponse
        onScreenLogger?.response = nimbusResponse
    }

    override fun onAdRendered(controller: AdController) {
        onScreenLogger = OnScreenLogger(adapter = adapter, response = response).also {
            controller.listeners.add(it)
        }
        controller.listeners.add(object : AdController.Listener {
            override fun onAdEvent(adEvent: AdEvent) {
                Timber.i("$identifier: ${adEvent.name}")
            }

            override fun onError(error: NimbusError) {
                Timber.e("$identifier: ${error.message}")
            }
        })
        onAdRenderedCallback(controller)
    }
}

class OnScreenLogger(val adapter: LogAdapter, var response: NimbusResponse?) : AdController.Listener {
    private var hasLoggedRendered = false
    override fun onAdEvent(adEvent: AdEvent) {
        adapter.submitList(buildList {
            addAll(adapter.currentList)
            if (!hasLoggedRendered && (adEvent == AdEvent.LOADED || adEvent == AdEvent.IMPRESSION)) {
                add("Rendered: ${response?.testDescription}")
                hasLoggedRendered = true
            }
            add("Event: ${adEvent.name}")
        })
    }

    override fun onError(error: NimbusError) {
        adapter.submitList(buildList {
            addAll(adapter.currentList)
            add("Error: ${error.errorType.name}" + error.message?.let { " - $it" })
        })
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

object EmptyAdControllerListenerImplementation : AdController.Listener {
    override fun onAdEvent(adEvent: AdEvent) { }

    override fun onError(error: NimbusError) { }
}

object UiTestInterceptor : Interceptor {
    override fun modifyAd(ad: NimbusAd): NimbusAd = ad

    override fun modifyController(ad: NimbusAd, controller: AdController): AdController = controller.apply {
        controller.setTestDescription(ad)
    }
}

class LogAdapter : ListAdapter<String, TextViewHolder>(object : ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem === newItem

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder = TextViewHolder(
        AppCompatTextView(parent.context)
    )

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        holder.view.text = getItem(position)
    }
}

fun RecyclerView.useAsLogger(logAdapter: LogAdapter = LogAdapter()) = apply {
    adapter = logAdapter
    layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false).apply { stackFromEnd = true }
}
