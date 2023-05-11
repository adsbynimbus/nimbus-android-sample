package com.adsbynimbus.android.sample.rendering

import android.view.Gravity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.adsbynimbus.render.AdController

fun AdController.alignTop() {
    view?.updateLayoutParams<FrameLayout.LayoutParams> {
        gravity = Gravity.TOP
        height = WRAP_CONTENT
    }
}
