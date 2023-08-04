package com.adsbynimbus.android.sample.rendering

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAd
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.databinding.LayoutTestBinding
import com.adsbynimbus.render.CompanionAd
import com.adsbynimbus.render.Renderer.Companion.loadBlockingAd

class TestRenderFragment : Fragment() {

    private val vastRegex = Regex("<vast", RegexOption.IGNORE_CASE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = LayoutTestBinding.inflate(inflater, container, false).apply {
        testButton.setOnClickListener {
            val markup = markupText.text!!.trim(' ', '\n')

            if (markup.isEmpty()) {
                AlertDialog.Builder(context)
                    .setTitle(getString(R.string.test_render_invalid_ad))
                    .setMessage(getString(R.string.test_render_invalid_ad_message)).show()
                return@setOnClickListener
            }

            val type = if (vastRegex.containsMatchIn(markup)) "video" else "static"

            requireContext().loadBlockingAd(object : NimbusAd {
                override fun type(): String = type

                override fun network(): String = "test_render"

                override fun markup(): String = markupText.text.toString().unescape()

                override fun companionAds(): Array<CompanionAd> = arrayOf(CompanionAd.end(320, 480))
            })?.start()
        }
    }.root

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
}
