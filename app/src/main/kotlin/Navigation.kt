package com.adsbynimbus.android.sample

import androidx.navigation.NavController
import androidx.navigation.createGraph
import androidx.navigation.fragment.fragment
import com.adsbynimbus.android.sample.test.TestRenderFragment

fun NavController.nimbusGraph() = createGraph(startDestination = "Public") {
    fragment<MainFragment>("Public") {
        label = context.getString(R.string.main_subtitle)
    }
    fragment<AdDemoFragment>("Show Ad Demo") {
        label = context.getString(R.string.ad_demo_subtitle)
    }
    fragment<MediationPlatformsFragment>("Mediation Platforms") {
        label = context.getString(R.string.mediation_subtitle)
    }
    fragment<ThirdPartyDemandFragment>("Third Party Demand") {
        label = context.getString(R.string.third_party_demand_subtitle)
    }
    fragment<TestRenderFragment>("Test Render") {
        label = context.getString(R.string.test_render_subtitle)
    }
    fragment<SettingsFragment>("Settings") {
        label = context.getString(R.string.settings_subtitle)
    }
}
