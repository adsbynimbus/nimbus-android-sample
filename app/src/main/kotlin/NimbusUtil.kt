package com.adsbynimbus.android.sample

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.databinding.CustomDialogBinding
import com.adsbynimbus.request.RequestManager
import com.adsbynimbus.request.VungleDemandProvider

/** A global instance of the NimbusAdManager */
val adManager: NimbusAdManager = NimbusAdManager()

/** A best guess algorithm for unescaping HTML markup that may be dumped from a server log */
fun String.unescape(): String = replace(Regex("\\s+"), " ")
    .replace("""\n""", " ")
    .replace("""\u003d""", "=")
    .replace("""\u003c""", "<")
    .replace("""\u003e""", ">")
    .replace("""\u0027""", "'")
    .replace("""\/""", "/")
    .replace("""\"""", """"""")
    .replace("""\t""", "    ")
    .replace("""\\""", """\""")
    .replace("""\ """, "")
    .trim()

const val tcfString =
    "CLcVDxRMWfGmWAVAHCENAXCkAKDAADnAABRgA5mdfCKZuYJez-NQm0TBMYA4oCAAGQYIAAAAAAEAIAEgAA.argAC0gAAAAAAAAAAAA"

var VungleDemandProvider.enabled: Boolean
    get() = RequestManager.interceptors.contains(this)
    set(enabled) = with(RequestManager.interceptors) {
        if (enabled) add(VungleDemandProvider) else remove(VungleDemandProvider)
    }

fun Context.showCustomDialog(message: String): AlertDialog = AlertDialog.Builder(this)
    .setCancelable(false)
    .create().apply {
        setView(CustomDialogBinding.inflate(LayoutInflater.from(this@showCustomDialog)).apply {
            description.text = getString(R.string.custom_dialog_message, message)
            button.setOnClickListener { dismiss() }
        }.root)
    }
