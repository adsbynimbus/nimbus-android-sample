package com.adsbynimbus.android.sample

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.common.Describable
import com.adsbynimbus.android.sample.databinding.LayoutInlineAdBinding
import com.adsbynimbus.android.sample.databinding.NavigationSecondaryBinding
import com.adsbynimbus.request.NimbusRequest
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

fun Describable.navigationBundle(subtitle: CharSequence) = bundleOf(
    "item" to this,
    "titleText" to description,
    "subtitleText" to subtitle,
)
