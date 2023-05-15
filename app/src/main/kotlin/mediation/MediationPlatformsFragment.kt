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
import com.adsbynimbus.android.sample.databinding.NavigationSecondaryBinding
import com.adsbynimbus.android.sample.navigationBundle

class MediationPlatformsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = NavigationSecondaryBinding.inflate(inflater, container, false).apply {
        headerTitle.text = resources.getString(R.string.mediation_title)
        headerSubtitle.text = resources.getString(R.string.mediation_subtitle)

        val googleSectionAdapter = SampleAppSectionAdapter(
            "mediationPlatforms",
            "Google",
            resources.getString(R.string.google),
        )
        val googleAdapter =
            SampleAppAdapter("mediationPlatforms", enumValues<MediationItem>()) {
                if (BuildConfig.GAM_PLACEMENT_ID.isEmpty()) {
                    showCustomDialog("GAM_PLACEMENT_ID", inflater, root.context).show()
                } else findNavController().navigate(R.id.to_GAMDemoFragment,
                    it.navigationBundle(subtitle = headerTitle.text)
                )
            }

        recyclerView.adapter = ConcatAdapter(googleSectionAdapter, googleAdapter)
        recyclerView.layoutManager = LinearLayoutManager(root.context)
    }.root
}
