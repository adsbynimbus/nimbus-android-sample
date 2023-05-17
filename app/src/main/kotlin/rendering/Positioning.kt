package com.adsbynimbus.android.sample.rendering

import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.adsbynimbus.render.AdController

inline fun AdController.align(alignment: () -> Int) {
    view?.updateLayoutParams<FrameLayout.LayoutParams> {
        gravity = alignment()
        height = WRAP_CONTENT
    }
}
