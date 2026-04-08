package com.adsbynimbus.android.sample.rendering

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import com.adsbynimbus.NimbusError
import com.adsbynimbus.android.nimbus.R
import com.adsbynimbus.android.sample.R.id.nimbus_ad_view
import com.adsbynimbus.android.sample.R.string.custom_dialog_message
import com.adsbynimbus.android.sample.TextViewHolder
import com.adsbynimbus.android.sample.databinding.CustomDialogBinding
import com.adsbynimbus.render.AdEvent
import com.adsbynimbus.render.internal.AdController
import com.adsbynimbus.render.internal.Interceptor
import com.adsbynimbus.request.NimbusResponse
import java.util.concurrent.CopyOnWriteArrayList

/** A Debug description of the Nimbus Response used for UI testing */
inline val NimbusResponse.testDescription
    get() = "${bid.ext.omp?.buyer ?: "none"} ${bid.mtype}" + if (bid.w != 0 && bid.h != 0) " ${bid.w}x${bid.h}" else ""

/** Sets debug information on the AdController for use with UI testing */
fun AdController.setTestDescription(response: NimbusResponse?) {
    view?.apply {
        if (id != R.id.nimbus_refreshing_controller) id = nimbus_ad_view
        contentDescription = response?.testDescription
    }
}

class ScreenAdLogger(
    val identifier: String,
    val logView: RecyclerView,
) {
    val adapter = logView.adapter as? LogAdapter ?: LogAdapter().also { logView.useAsLogger(it) }
    private var hasLoggedRendered = false
    fun onAdEvent(adEvent: AdEvent) {
        if (!hasLoggedRendered && (adEvent == AdEvent.LOADED || adEvent == AdEvent.IMPRESSION)) {
            adapter.appendLog("Rendered: $identifier")
            hasLoggedRendered = true
        }
        adapter.appendLog("Event: ${adEvent.name}")
    }

    fun onError(error: NimbusError) {
        adapter.appendLog("Error: ${error.errorType.name}" + error.message?.let { " - $it" })
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
    override fun modifyAd(ad: NimbusResponse): NimbusResponse = ad

    override fun modifyController(ad: NimbusResponse, controller: AdController): AdController =
        controller.apply {
            controller.setTestDescription(ad)
        }
}

class LogAdapter : ListAdapter<String, TextViewHolder>(object : ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem === newItem

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
}) {
    val messageList = CopyOnWriteArrayList<String>()

    fun appendLog(message: String) {
        messageList.add(message)
        submitList(messageList)
        notifyItemInserted(messageList.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder =
        TextViewHolder(
            AppCompatTextView(parent.context)
        )

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        holder.view.text = getItem(position)
    }

    fun clear() {
        messageList.clear()
        submitList(messageList)
    }
}

fun RecyclerView.useAsLogger(logAdapter: LogAdapter) = apply {
    adapter = logAdapter
    layoutManager =
        LinearLayoutManager(context, RecyclerView.VERTICAL, false).apply { stackFromEnd = true }
}
