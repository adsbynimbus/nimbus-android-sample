package com.adsbynimbus.android.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adsbynimbus.android.sample.common.Describable
import com.adsbynimbus.android.sample.common.SampleAppAdapter
import com.adsbynimbus.android.sample.databinding.NavigationPrimaryBinding

class NavigationAdapter(val items: List<String>) : RecyclerView.Adapter<NavigationAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent)

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}

class MainFragment : Fragment() {

    enum class Item(
        override val description: String
    ) : Describable {
        SHOW_AD_DEMO("Show Ad Demo"),
        MEDIATION_PLATFORMS("Mediation Platforms"),
        THIRD_PARTY("Third Party Demand"),
        TEST_RENDER("Test Render"),
        SETTINGS("Settings"),
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = NavigationPrimaryBinding.inflate(inflater, container, false).apply {
        recyclerView.adapter = SampleAppAdapter("main", enumValues<Item>()) { item ->
            val action = when (item) {
                Item.SHOW_AD_DEMO -> R.id.to_adDemoFragment
                Item.MEDIATION_PLATFORMS -> R.id.to_mediationPlatformsFragment
                Item.THIRD_PARTY -> R.id.to_thirdPartyDemand
                Item.TEST_RENDER -> R.id.to_testRenderFragment
                Item.SETTINGS -> R.id.to_settingsFragment
            }
            findNavController().navigate(action)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
    }.root
}
