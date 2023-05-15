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
import com.adsbynimbus.android.sample.admanager.FANAdItem
import com.adsbynimbus.android.sample.admanager.VungleAdItem
import com.adsbynimbus.android.sample.common.SampleAppAdapter
import com.adsbynimbus.android.sample.common.SampleAppSectionAdapter
import com.adsbynimbus.android.sample.common.showCustomDialog
import com.adsbynimbus.android.sample.databinding.NavigationSecondaryBinding
import com.adsbynimbus.android.sample.navigationBundle

class ThirdPartyDemandFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = NavigationSecondaryBinding.inflate(inflater, container, false).apply {
        headerTitle.text = resources.getString(R.string.third_party_demand_title)
        headerSubtitle.text = resources.getString(R.string.third_party_demand_subtitle)

        val unitySectionAdapter = SampleAppSectionAdapter(
            "thirdPartyDemand",
            "Unity",
            resources.getString(R.string.unity),
        )
        val adItemAdapter = SampleAppAdapter("thirdPartyDemand", enumValues<UnityAdItem>()) {
            if (BuildConfig.UNITY_GAME_ID.isEmpty()) {
                showCustomDialog("UNITY_GAME_ID", inflater, root.context).show()
            } else {
                findNavController().navigate(R.id.to_unityDemo,
                    it.navigationBundle(subtitle = headerTitle.text)
                )
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
            findNavController().navigate(R.id.to_apsDemoFragment,
                it.navigationBundle(subtitle = headerTitle.text)
            )
        }

        val fanSectionAdapter = SampleAppSectionAdapter(
            "thirdPartyDemand",
            "FAN",
            resources.getString(R.string.fan),
        )
        val fanAdItemAdapter = SampleAppAdapter("thirdPartyDemand", enumValues<FANAdItem>()) {
            val adUnitId = when (it) {
                FANAdItem.BANNER -> BuildConfig.FAN_BANNER_320_ID
                FANAdItem.INTERSTITIAL -> BuildConfig.FAN_INTERSTITIAL_ID
                FANAdItem.NATIVE -> BuildConfig.FAN_NATIVE_ID.ifEmpty { BuildConfig.FAN_NATIVE_320_ID }
            }
            if (adUnitId.isEmpty()) {
                showCustomDialog(it.gradlePropertyName, inflater, root.context).show()
            } else {
                findNavController().navigate(R.id.to_FANDemoFragment,
                    it.navigationBundle(subtitle = headerTitle.text)
                )
            }
        }

        val vungleSectionAdapter = SampleAppSectionAdapter(
            "thirdPartyDemand",
            "Vungle",
            resources.getString(R.string.vungle),
        )

        val vungleAdItemAdapter = SampleAppAdapter("vungle", enumValues<VungleAdItem>()) {
            val adUnitId = when (it) {
                VungleAdItem.BANNER -> BuildConfig.VUNGLE_BANNER_320_ID
                VungleAdItem.MREC -> BuildConfig.VUNGLE_MREC_ID
                VungleAdItem.INTERSTITIAL -> BuildConfig.VUNGLE_INTERSTITIAL_ID
                VungleAdItem.REWARDED -> BuildConfig.VUNGLE_REWARDED_ID
            }
            if (adUnitId.isEmpty()) {
                showCustomDialog(it.gradlePropertyName, inflater, root.context).show()
            } else {
                findNavController().navigate(R.id.to_vungleDemo,
                    it.navigationBundle(subtitle = "Vungle Ads")
                )
            }
        }

        recyclerView.adapter = ConcatAdapter(unitySectionAdapter, adItemAdapter, apsSectionAdapter, apsItemAdapter, fanSectionAdapter, fanAdItemAdapter, vungleSectionAdapter, vungleAdItemAdapter)
        recyclerView.layoutManager = LinearLayoutManager(root.context)
    }.root
}
