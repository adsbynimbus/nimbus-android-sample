package com.adsbynimbus.android.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.adsbynimbus.android.sample.databinding.NavigationSecondaryBinding

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = RecyclerView(requireContext()).apply {
        adapter = NavigationAdapter(items = listOf(
            Destination("Show Ad Demo"),
            Destination("Mediation Platforms"),
            Destination("Third Party Demand"),
            Destination("Test Render"),
            Destination("Settings"),
        ))
    }
}

class AdDemoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = NavigationSecondaryBinding.inflate(inflater, container, false).apply {
        recyclerView.adapter = NavigationAdapter(items = listOf(
            Destination("Manual Request/Render Ad"),
            Destination("Banner"),
            Destination("Interstitial Static"),
            Destination("Interstitial Video"),
            Destination("Interstitial Hybrid"),
            Destination("Blocking Interstitial (5 sec)"),
            Destination("Rewarded Static (5 sec)"),
            Destination("Rewarded Video (5 sec)"),
            Destination("Ads in ScrollView")
        ))
    }.root
}

class ThirdPartyDemandFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = NavigationSecondaryBinding.inflate(inflater, container, false).apply {
        recyclerView.adapter = NavigationAdapter(items = listOf(
            Header("Unity"),
            Destination("Rewarded Video Unity (5 sec)"),
            Header("APS"),
            Destination("APS Banner"),
            Destination("APS Interstitial Hybrid"),
            Header("Meta Audience Network"),
            Destination("Meta Banner"),
            Destination("Meta Interstitial"),
            Destination("Meta Native"),
            Header("Vungle"),
            Destination("Vungle Banner"),
            Destination("Vungle MREC"),
            Destination("Vungle Interstitial"),
            Destination("Vungle Rewarded"),
        ))
    }.root
}

class MediationPlatformsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = NavigationSecondaryBinding.inflate(inflater, container, false).apply {
        recyclerView.adapter = NavigationAdapter(items = listOf(
            Header("Google"),
            Destination("Banner"),
            Destination("Dynamic Price Banner"),
            Destination("Interstitial"),
            Destination("Dynamic Price Interstitial"),
            Destination("Dynamic Price Interstitial Static"),
            Destination("Dynamic Price Interstitial Video"),
        ))
    }.root
}
