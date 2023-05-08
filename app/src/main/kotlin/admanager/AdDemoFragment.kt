package com.adsbynimbus.android.sample.admanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.common.SampleAppAdapter
import com.adsbynimbus.android.sample.databinding.FragmentAdDemoBinding

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
            findNavController().navigate(R.id.to_adManagerFragment, bundleOf(
                "item" to item,
                "titleText" to item.description,
                "subtitleText" to titleText,
            ))
        }

        recyclerView.adapter = adItemAdapter
        recyclerView.layoutManager = LinearLayoutManager(root.context)
    }.root
}
