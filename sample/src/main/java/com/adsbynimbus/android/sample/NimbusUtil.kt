package com.adsbynimbus.android.sample

import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.request.NimbusRequest

/** A global instance of the NimbusAdManager */
val adManager: NimbusAdManager = NimbusAdManager()

/** Shows how to make a video only request to Nimbus */
fun forVideoAd(
    position: String = "test_video",
    fullScreen: Boolean = false
): NimbusRequest = NimbusRequest.forRewardedVideo(position).apply {
    request.imp[0].instl = if (fullScreen) 1 else 0
}

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

