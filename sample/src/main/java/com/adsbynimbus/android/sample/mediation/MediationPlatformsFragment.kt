package com.adsbynimbus.android.sample.mediation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.adsbynimbus.android.sample.BuildConfig
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.common.SampleAppAdapter
import com.adsbynimbus.android.sample.common.SampleAppSectionAdapter
import com.adsbynimbus.android.sample.common.showCustomDialog
import com.adsbynimbus.android.sample.databinding.FragmentMediationPlatformsBinding

class MediationPlatformsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentMediationPlatformsBinding.inflate(inflater, container, false).apply {
        val titleText: String = resources.getString(R.string.mediation_title)
        val subtitleText: String = resources.getString(R.string.mediation_subtitle)

        headerView.setTitleText(titleText)
        headerView.setSubtitleText(subtitleText)

        val googleSectionAdapter = SampleAppSectionAdapter(
            "mediationPlatforms",
            "Google",
            resources.getString(R.string.google),
        )
        val googleAdapter =
            SampleAppAdapter("mediationPlatforms", enumValues<MediationItem>()) { item ->
                if (BuildConfig.GAM_PLACEMENT_ID.isEmpty()) {
                    showCustomDialog(
                        "GAM_PLACEMENT_ID",
                        inflater,
                        this@MediationPlatformsFragment.context
                    ).show()
                } else {
                    val action =
                        MediationPlatformsFragmentDirections.toGAMDemoFragment(
                            item,
                            item.description,
                            "Google - $titleText"
                        )
                    findNavController().navigate(action)
                }
            }

        val mopubSectionAdapter = SampleAppSectionAdapter(
            "mediationPlatforms",
            "Mopub",
            resources.getString(R.string.mopub),
        )
        val mopubAdapter =
            SampleAppAdapter("mediationPlatforms", enumValues<MediationItem>()) { item ->
                if (
                    (item == MediationItem.BANNER || item == MediationItem.DYNAMIC_PRICE_BANNER) &&
                    BuildConfig.MOPUB_BANNER_ID.isEmpty()
                ) {
                    showCustomDialog("MOPUB_BANNER_ID", inflater, context).show()
                } else if (
                    (item == MediationItem.INTERSTITIAL || item == MediationItem.DYNAMIC_PRICE_INTERSTITIAL) &&
                    BuildConfig.MOPUB_INTERSTITIAL_ID.isEmpty()
                ) {
                    showCustomDialog("MOPUB_INTERSTITIAL_ID", inflater, context).show()
                } else {
                    val action =
                        MediationPlatformsFragmentDirections.toMoPubFragment(
                            item,
                            item.description,
                            "MoPub - $titleText"
                        )
                    findNavController().navigate(action)
                }
            }

        val concatAdapter = ConcatAdapter(
            googleSectionAdapter,
            googleAdapter,
            mopubSectionAdapter,
            mopubAdapter,
        )
        recyclerView.adapter = concatAdapter
        recyclerView.layoutManager = LinearLayoutManager(this@MediationPlatformsFragment.context)
    }.root
}
