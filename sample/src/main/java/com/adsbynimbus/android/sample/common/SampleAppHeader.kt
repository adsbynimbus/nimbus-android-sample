package com.adsbynimbus.android.sample.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.databinding.LayoutHeaderSampleAppBinding

class SampleAppHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var titleText = ""
    private var subtitleText = ""

    private var binding = LayoutHeaderSampleAppBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SampleAppHeader,
            defStyleAttr,
            defStyleRes
        ).apply {
            try {
                getString(R.styleable.SampleAppHeader_titleText)?.let {
                    titleText = it
                }
                getString(R.styleable.SampleAppHeader_subtitleText)?.let {
                    subtitleText = it
                }
            } finally {
                recycle()
            }
        }

        binding.headerTitle.text = titleText
        binding.headerSubtitle.text = subtitleText
    }

    fun setTitleText(titleText: String) {
        this@SampleAppHeader.titleText = titleText
        binding.headerTitle.text = titleText
        invalidate()
        requestLayout()
    }

    fun setSubtitleText(subtitleText: String) {
        this@SampleAppHeader.subtitleText = subtitleText
        binding.headerSubtitle.text = subtitleText
        invalidate()
        requestLayout()
    }
}