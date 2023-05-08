package com.adsbynimbus.android.sample.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.adsbynimbus.NimbusAd
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.databinding.FragmentTestRenderBinding
import com.adsbynimbus.android.sample.unescape
import com.adsbynimbus.render.CompanionAd
import com.adsbynimbus.render.Renderer.Companion.loadBlockingAd

class TestRenderFragment : Fragment() {

    private val vastRegex = Regex("<vast", RegexOption.IGNORE_CASE)
    private val htmlRegex = Regex("<html", RegexOption.IGNORE_CASE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentTestRenderBinding.inflate(inflater, container, false).apply {
        val titleText: String = resources.getString(R.string.test_render_title)
        val subtitleText: String = resources.getString(R.string.test_render_subtitle)

        headerView.setTitleText(titleText)
        headerView.setSubtitleText(subtitleText)

        testButton.setOnClickListener {
            val type = when {
                vastRegex.containsMatchIn(markupEditText.text!!) -> "video"
                htmlRegex.containsMatchIn(markupEditText.text!!) -> "static"
                else -> return@setOnClickListener
            }

            requireContext().loadBlockingAd(object : NimbusAd {
                override fun type(): String = type

                override fun markup(): String = markupEditText.text.toString().unescape()

                override fun companionAds(): Array<CompanionAd> = arrayOf(CompanionAd.end(320, 480))
            })?.start()
        }
    }.root
}
