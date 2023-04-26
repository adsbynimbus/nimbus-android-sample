package com.adsbynimbus.android.sample.demand

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
import com.adsbynimbus.android.sample.admanager.APSAdItem
import com.adsbynimbus.android.sample.admanager.AdItem
import com.adsbynimbus.android.sample.admanager.FANAdItem
import com.adsbynimbus.android.sample.admanager.VungleAdItem
import com.adsbynimbus.android.sample.common.SampleAppAdapter
import com.adsbynimbus.android.sample.common.SampleAppSectionAdapter
import com.adsbynimbus.android.sample.common.showCustomDialog
import com.adsbynimbus.android.sample.databinding.FragmentAdDemoBinding

class ThirdPartyDemandFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentAdDemoBinding.inflate(inflater, container, false).apply {
        val titleText: String = resources.getString(R.string.third_party_demand_title)
        headerView.setTitleText(titleText)
        headerView.setSubtitleText(resources.getString(R.string.third_party_demand_subtitle))

        val unitySectionAdapter = SampleAppSectionAdapter(
            "thirdPartyDemand",
            "Unity",
            resources.getString(R.string.unity),
        )
        val adItemAdapter = SampleAppAdapter("thirdPartyDemand", arrayOf(AdItem.REWARDED_VIDEO_UNITY)) { item ->
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

        val apsSectionAdapter = SampleAppSectionAdapter(
            "thirdPartyDemand",
            "APS",
            resources.getString(R.string.aps),
        )
        val apsItemAdapter = SampleAppAdapter("thirdPartyDemand", enumValues<APSAdItem>()) {
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

        val fanSectionAdapter = SampleAppSectionAdapter(
            "thirdPartyDemand",
            "FAN",
            resources.getString(R.string.fan),
        )
        val fanAdItemAdapter = SampleAppAdapter("thirdPartyDemand", enumValues<FANAdItem>()) { item ->
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

        val vungleSectionAdapter = SampleAppSectionAdapter(
            "thirdPartyDemand",
            "Vungle",
            resources.getString(R.string.vungle),
        )

        val vungleAdItemAdapter = SampleAppAdapter("vungle", enumValues<VungleAdItem>()) { item ->
            val adUnitId = when (item) {
                VungleAdItem.BANNER -> BuildConfig.VUNGLE_BANNER_320_ID
                VungleAdItem.MREC -> BuildConfig.VUNGLE_MREC_ID
                VungleAdItem.INTERSTITIAL -> BuildConfig.VUNGLE_INTERSTITIAL_ID
                VungleAdItem.REWARDED -> BuildConfig.VUNGLE_REWARDED_ID
            }
            if (adUnitId.isEmpty()) {
                showCustomDialog(item.gradlePropertyName, inflater, root.context).show()
            } else {
                findNavController().navigate(
                    R.id.to_vungleDemo, bundleOf(
                        "item" to item,
                        "titleText" to item.description,
                        "subtitleText" to "Vungle Ads"
                    )
                )
            }
        }

        recyclerView.adapter = ConcatAdapter(unitySectionAdapter, adItemAdapter, apsSectionAdapter, apsItemAdapter, fanSectionAdapter, fanAdItemAdapter, vungleSectionAdapter, vungleAdItemAdapter)
        recyclerView.layoutManager = LinearLayoutManager(root.context)
    }.root
}
