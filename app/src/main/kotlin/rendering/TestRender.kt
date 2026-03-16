package com.adsbynimbus.android.sample.rendering

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.databinding.LayoutTestBinding
import com.adsbynimbus.render.Renderer.Companion.loadBlockingAd
import com.adsbynimbus.request.*

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

            val type = if (vastRegex.containsMatchIn(markup)) MarkupType.VIDEO else MarkupType.BANNER

            requireContext().loadBlockingAd(nimbusResponseFrom(type, markup.toString()), isRewarded = false)
                ?.start()
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

fun nimbusResponseFrom(
    type: MarkupType = MarkupType.BANNER,
    markup: String,
    width: Int = 0,
    height: Int = 0,
): NimbusResponse =
    NimbusResponse(
        "test",
        listOf(
            SeatBid(
                listOf(
                    Bid(
                        mtype = type,
                        adm = markup,
                        w = width,
                        h = height,
                    ),
                ),
            ),
        ),
    )
