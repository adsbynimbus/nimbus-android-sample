package com.adsbynimbus.android.sample

import androidx.navigation.NavController
import androidx.navigation.createGraph
import androidx.navigation.fragment.fragment
import com.adsbynimbus.android.sample.test.TestRenderFragment

fun NavController.nimbusGraph() = createGraph(startDestination = "Public") {
    fragment<MainFragment>("Public")
    fragment<AdDemoFragment>("Show Ad Demo")
    fragment<MediationPlatformsFragment>("Mediation Platforms")
    fragment<ThirdPartyDemandFragment>("Third Party Demand")
    fragment<TestRenderFragment>("Test Render")
    fragment<SettingsFragment>("Settings")
}
