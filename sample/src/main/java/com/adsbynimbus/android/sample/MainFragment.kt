package com.adsbynimbus.android.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.adsbynimbus.android.sample.common.SampleAppAdapter
import com.adsbynimbus.android.sample.databinding.FragmentMainBinding
import com.adsbynimbus.android.sample.common.Describable

class MainFragment : Fragment() {

    enum class Item(
        override val description: String
    ) : Describable {
        SHOW_AD_DEMO("Show Ad Demo"),
        MEDIATION_PLATFORMS("Mediation Platforms"),
        TEST_RENDER("Test Render"),
        SETTINGS("Settings"),
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentMainBinding.inflate(inflater, container, false).apply {
        recyclerView.adapter = SampleAppAdapter("main", enumValues<Item>()) { item ->
            val action = when (item) {
                Item.SHOW_AD_DEMO -> MainFragmentDirections.toAdDemoFragment()
                Item.MEDIATION_PLATFORMS -> MainFragmentDirections.toMediationPlatformsFragment()
                Item.TEST_RENDER -> MainFragmentDirections.toTestRenderFragment()
                Item.SETTINGS -> MainFragmentDirections.toSettingsFragment()
            }
            findNavController().navigate(action)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
    }.root
}
