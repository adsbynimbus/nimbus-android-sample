package com.adsbynimbus.android.sample.common

import android.content.Context
import android.util.AttributeSet
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreferenceCompat
import com.adsbynimbus.android.sample.R

class SampleSwitchPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : SwitchPreferenceCompat(context, attrs, defStyleAttr, defStyleRes) {

    private var titleContentDescription = ""
    private var switchWidgetContentDescription = ""

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SampleSwitchPreference,
            defStyleAttr,
            defStyleRes,
        ).apply {
            try {
                getString(R.styleable.SampleSwitchPreference_titleContentDescription)?.let {
                    titleContentDescription = it
                }
                getString(R.styleable.SampleSwitchPreference_switchWidgetContentDescription)?.let {
                    switchWidgetContentDescription = it
                }
            } finally {
                recycle()
            }
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.findViewById(android.R.id.title)?.let {
            it.contentDescription = titleContentDescription
        }
        holder.findViewById(R.id.switchWidget)?.let {
            it.contentDescription = switchWidgetContentDescription
        }
    }
}
