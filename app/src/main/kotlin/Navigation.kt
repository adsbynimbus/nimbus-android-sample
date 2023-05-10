package com.adsbynimbus.android.sample

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphBuilder
import androidx.navigation.createGraph
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.fragment
import androidx.navigation.navigation
import androidx.recyclerview.widget.RecyclerView
import com.adsbynimbus.android.sample.test.TestRenderFragment

val screens = mutableMapOf(
    "Main" to listOf(
        Destination("Show Ad Demo"),
        Destination("Mediation Platforms"),
        Destination("Third Party Demand"),
        Destination("Test Render"),
        Destination("Settings"),
    ),
    "Show Ad Demo" to listOf(
        Destination("Manual Request/Render Ad"),
        Destination("Banner"),
        Destination("Interstitial Static"),
        Destination("Interstitial Video"),
        Destination("Interstitial Hybrid"),
        Destination("Blocking Interstitial (5 sec)"),
        Destination("Rewarded Static (5 sec)"),
        Destination("Rewarded Video (5 sec)"),
        Destination("Ads in ScrollView")
    ),
    "Mediation Platforms" to listOf(
        Header("Google"),
        Destination("Banner"),
        Destination("Dynamic Price Banner"),
        Destination("Interstitial"),
        Destination("Dynamic Price Interstitial"),
        Destination("Dynamic Price Interstitial Static"),
        Destination("Dynamic Price Interstitial Video"),
    ),
    "Third Party Demand" to listOf(
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
    )
)

fun NavGraphBuilder.nimbusGraph(context: Context) = apply {
    fragment<NavigationFragment>("Main") {
        label = context.getString(R.string.main_subtitle)
    }
    fragment<NavigationFragment>("Show Ad Demo") {
        label = context.getString(R.string.ad_demo_subtitle)
    }
    fragment<NavigationFragment>("Mediation Platforms") {
        label = context.getString(R.string.mediation_subtitle)
    }
    fragment<NavigationFragment>("Third Party Demand") {
        label = context.getString(R.string.third_party_demand_subtitle)
    }
    fragment<TestRenderFragment>("Test Render") {
        label = context.getString(R.string.test_render_subtitle)
    }
    fragment<SettingsFragment>("Settings") {
        label = context.getString(R.string.settings_subtitle)
    }
}

var appGraph: NavController.() -> NavGraph = { createGraph(startDestination = "Main") { nimbusGraph(context) } }

class NavigationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = RecyclerView(requireContext()).apply {
        adapter = NavigationAdapter(items = screens[findNavController().currentDestination?.route] ?: emptyList())
    }
}
