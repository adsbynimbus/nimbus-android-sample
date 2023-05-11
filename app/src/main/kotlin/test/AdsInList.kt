package com.adsbynimbus.android.sample.test

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adsbynimbus.NimbusAdManager
import com.adsbynimbus.android.sample.demand.mockMetaNimbusAd
import com.adsbynimbus.openrtb.request.Format
import com.adsbynimbus.render.AdController
import com.adsbynimbus.render.Renderer
import com.adsbynimbus.request.NimbusRequest
import com.adsbynimbus.request.RequestManager

class ScrollingDemoFragment : Fragment(), NimbusRequest.Interceptor {

    val adManager: NimbusAdManager = NimbusAdManager()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = RecyclerView(requireContext()).apply {
        adapter = ScrollingAdapter()
        layoutManager = LinearLayoutManager(context)
        setItemViewCacheSize(6)
        addItemDecoration(object : RecyclerView.ItemDecoration() {
            val margin: Int = resources.displayMetrics.heightPixels / 10
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(0, margin, 0, margin)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        RequestManager.interceptors.add(this)
    }

    override fun onStop() {
        super.onStop()
        RequestManager.interceptors.remove(this)
    }

    override fun modifyRequest(request: NimbusRequest) {
        request.request.user?.apply {
            buyeruid = null
            ext?.unity_buyeruid = null
        }
    }

    class ViewHolder(val view: FrameLayout) : RecyclerView.ViewHolder(view) {
        var adController: AdController? = null
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
            if (holder.adController == null) when (position) {
                0 -> NimbusRequest.forBannerAd("test_banner_320", Format.BANNER_320_50, 0)
                1 -> NimbusRequest.forBannerAd("test_banner_300", Format.LETTERBOX, 0)
                2 -> NimbusRequest.forBannerAd("test_banner_interstitial_port",
                    Format.INTERSTITIAL_PORT,
                    0)
                3 -> NimbusRequest.forBannerAd("test_banner_interstitial_land",
                    Format.INTERSTITIAL_LAND,
                    0)
                4 -> NimbusRequest.forVideoAd("test_video")
                else -> {
                    mockMetaNimbusAd("Meta Native").let {
                        Renderer.loadAd(it, holder.view, object : NimbusAdManager.Listener {
                            override fun onAdRendered(controller: AdController) {
                                holder.adController = controller
                            }
                        })
                    }
                    return
                }
            }.let {
                adManager.showAd(it, holder.view) { controller ->
                    holder.adController = controller.apply { volume = 100 }
                }
            }
        }

        override fun getItemCount(): Int = 6

        override fun getItemViewType(position: Int): Int = if (position == 5) 1 else 0

        override fun onViewRecycled(holder: ViewHolder) {
            holder.adController?.destroy()
        }
    }
}
