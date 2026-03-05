package com.adsbynimbus.android.sample.rendering

import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adsbynimbus.*
import com.adsbynimbus.android.sample.demand.mockMetaNimbusAdPosition
import com.adsbynimbus.openrtb.request.Format
import kotlinx.coroutines.launch

class ScrollingDemoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = RecyclerView(requireContext()).apply {
        adapter = ScrollingAdapter()
        layoutManager = LinearLayoutManager(context)
        disableAllExtensions()
        setItemViewCacheSize(6)
        addItemDecoration(object : RecyclerView.ItemDecoration() {
            val margin: Int = resources.displayMetrics.heightPixels / 10
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(0, margin, 0, margin)
            }
        })
    }

    class ViewHolder(val view: FrameLayout) : RecyclerView.ViewHolder(view) {
        var ad: Ad? = null
    }

    inner class ScrollingAdapter : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(FrameLayout(parent.context).apply {
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    if (viewType == 0) LayoutParams.WRAP_CONTENT else parent.width
                )
            })

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (holder.ad == null) when (position) {
                0 -> Nimbus.bannerAd("test_banner_320", Format.BANNER_320_50)
                1 -> Nimbus.bannerAd("test_banner_300", Format.LETTERBOX)
                2 -> Nimbus.bannerAd("test_banner_interstitial_port", Format.INTERSTITIAL_PORT)
                3 -> Nimbus.bannerAd("test_banner_interstitial_land", Format.INTERSTITIAL_LAND,)
                4 -> Nimbus.inlineAd("test_video") { video() }
                else -> Nimbus.nativeAd(
                            mockMetaNimbusAdPosition(
                                "Meta Native",
                                { requireActivity().showPropertyMissingDialog(it) },
                            ),
                        )
            }.let {
                viewLifecycleOwner.lifecycleScope.launch {
                    holder.ad = it
                    it.show(holder.view)
                }
            }
        }

        override fun getItemCount(): Int = 6

        override fun getItemViewType(position: Int): Int = if (position == 5) 1 else 0

        override fun onViewRecycled(holder: ViewHolder) {
            holder.ad?.destroy()
        }
    }
}
