package com.adsbynimbus.android.sample.admanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.common.SampleAppAdapter
import com.adsbynimbus.android.sample.common.showCustomDialog
import com.adsbynimbus.android.sample.databinding.FragmentAdDemoBinding
import com.amazon.aps.ads.ApsAd

class AdDemoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentAdDemoBinding.inflate(inflater, container, false).apply {
        val titleText: String = resources.getString(R.string.ad_demo_title)
        headerView.setTitleText(titleText)
        headerView.setSubtitleText(resources.getString(R.string.ad_demo_subtitle))

        val adItemAdapter = SampleAppAdapter("showAdDemo", enumValues<AdItem>()) { item ->
            if (item == AdItem.REWARDED_VIDEO_UNITY && BuildConfig.UNITY_GAME_ID.isEmpty()) {
                showCustomDialog("UNITY_GAME_ID", inflater, root.context).show()
            } else {
                findNavController().navigate(R.id.to_adManagerFragment, bundleOf(
                    "item" to item,
                    "titleText" to item.description,
                    "subtitleText" to titleText,
                ))
            }
        }

        val apsItemAdapter = SampleAppAdapter("showAdDemo", enumValues<APSAdItem>()) {
            when (it) {
                APSAdItem.BANNER -> if (BuildConfig.APS_BANNER.isEmpty()) {
                    showCustomDialog("sample_aps_banner", inflater, root.context).show()
                    return@SampleAppAdapter
                }
                APSAdItem.INTERSTITIAL_HYBRID -> {
                    val keysNotConfigured = BuildConfig.APS_STATIC.isEmpty().also { empty ->
                        if (empty) showCustomDialog("sample_aps_static", inflater, root.context).show()
                    } and BuildConfig.APS_VIDEO.isEmpty().also { empty ->
                        if (empty) showCustomDialog("sample_aps_video", inflater, root.context).show()
                    }
                    if (keysNotConfigured) return@SampleAppAdapter
                }
            }
            findNavController().navigate(R.id.to_apsDemoFragment, bundleOf(
                "item" to it,
                "titleText" to it.description,
                "subtitleText" to titleText,
            ))
        }

        val fanAdItemAdapter = SampleAppAdapter("showAdDemo", enumValues<FANAdItem>()) { item ->
            val adUnitId = when (item) {
                FANAdItem.BANNER -> BuildConfig.FAN_BANNER_320_ID
                FANAdItem.INTERSTITIAL -> BuildConfig.FAN_INTERSTITIAL_ID
                FANAdItem.NATIVE -> BuildConfig.FAN_NATIVE_ID.ifEmpty { BuildConfig.FAN_NATIVE_320_ID }
            }
            if (adUnitId.isEmpty()) {
                showCustomDialog(item.gradlePropertyName, inflater, root.context).show()
            } else {
                findNavController().navigate(R.id.to_FANDemoFragment, bundleOf(
                    "item" to item,
                    "titleText" to item.description,
                    "subtitleText" to titleText,
                ))
            }
        }
        recyclerView.adapter = ConcatAdapter(adItemAdapter, apsItemAdapter, fanAdItemAdapter)
        recyclerView.layoutManager = LinearLayoutManager(root.context)
    }.root
}
