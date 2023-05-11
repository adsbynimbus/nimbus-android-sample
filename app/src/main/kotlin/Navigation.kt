package com.adsbynimbus.android.sample

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.createGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adsbynimbus.android.sample.admanager.AdManagerFragment
import com.adsbynimbus.android.sample.demand.*
import com.adsbynimbus.android.sample.mediation.GAMDemoFragment
import com.adsbynimbus.android.sample.test.TestRenderFragment

val screens = mutableMapOf(
    "Main" to listOf(
        Demos("Show Ad Demo"),
        Demos("Mediation Platforms"),
        Demos("Third Party Demand"),
        Demos("Test Render"),
        Demos("Settings"),
    ),
    "Show Ad Demo" to listOf(
        AdManager("Manually Rendered Ad"),
        AdManager("Banner"),
        AdManager("Interstitial Static"),
        AdManager("Interstitial Video"),
        AdManager("Interstitial Hybrid"),
        AdManager("Blocking Interstitial (5 sec)"),
        AdManager("Rewarded Static (5 sec)"),
        AdManager("Rewarded Video (5 sec)"),
        AdManager("Ads in ScrollView")
    ),
    "Mediation Platforms" to listOf(
        Header("Google"),
        Google("Banner"),
        Google("Interstitial"),
        Google("Dynamic Price Banner"),
        Google("Dynamic Price Interstitial"),
        Google("Dynamic Price Interstitial Static"),
        Google("Dynamic Price Interstitial Video"),
    ),
    "Third Party Demand" to listOf(
        Header("Unity"),
        Unity("Rewarded Video Unity (5 sec)"),
        Header("APS"),
        APS("APS Banner"),
        APS("APS Interstitial Hybrid"),
        Header("Meta Audience Network"),
        Meta("Meta Banner"),
        Meta("Meta Interstitial"),
        Meta("Meta Native"),
        Header("Vungle"),
        Vungle("Vungle Banner"),
        Vungle("Vungle MREC"),
        Vungle("Vungle Interstitial"),
        Vungle("Vungle Rewarded"),
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
    fragment<AdManagerFragment>("Show Ad Demo/{item}") { argument("item") { type = NavType.StringType } }
    fragment<APSFragment>("APS/{item}") { argument("item") { type = NavType.StringType } }
    fragment<GAMDemoFragment>("Google/{item}") { argument("item") { type = NavType.StringType } }
    fragment<MetaFragment>("Meta/{item}") { argument("item") { type = NavType.StringType } }
    fragment<UnityFragment>("Unity/{item}") { argument("item") { type = NavType.StringType } }
    fragment<VungleFragment>("Vungle/{item}") { argument("item") { type = NavType.StringType } }
}

var appGraph: NavController.() -> NavGraph = { createGraph(startDestination = "Main") { nimbusGraph(context) } }

sealed interface NavItem {
    val text: String
}

interface Decoration : NavItem
interface Destination : NavItem

@JvmInline value class Demos(val route: String): Destination { override val text: String get() = route }
@JvmInline value class AdManager(val demo: String): Destination { override val text: String get() = demo }
@JvmInline value class APS(val demo: String): Destination { override val text: String get() = demo }
@JvmInline value class Google(val demo: String): Destination { override val text: String get() = demo }
@JvmInline value class Meta(val demo: String): Destination { override val text: String get() = demo }
@JvmInline value class Unity(val demo: String): Destination { override val text: String get() = demo }
@JvmInline value class Vungle(val demo: String): Destination { override val text: String get() = demo }
@JvmInline value class Header(val name: String): Decoration { override val text: String get() = name }

class NavigationFragment : Fragment() {

    inline val navController get() = findNavController()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = RecyclerView(requireContext()).apply {
        adapter = NavigationAdapter(items = screens[navController.currentDestination?.route] ?: emptyList())
    }
}

class NavigationAdapter(val items: List<NavItem>) : RecyclerView.Adapter<NavigationAdapter.ViewHolder>() {

    class ViewHolder(val view: TextView) : RecyclerView.ViewHolder(view)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, LinearLayout.VERTICAL))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false) as TextView).apply {
            view.setOnClickListener {
                it.findNavController().apply {
                    when (val screen = items[bindingAdapterPosition] as Destination) {
                        is Demos -> navigate(screen.text)
                        is AdManager -> navigate("Show Ad Demo/${screen.demo}")
                        is APS -> navigate("APS/${screen.demo}")
                        is Google -> navigate("Google/${screen.demo}")
                        is Meta -> navigate("Meta/${screen.demo}")
                        is Unity -> navigate("Unity/${screen.demo}")
                        is Vungle -> navigate("Vungle/${screen.demo}")
                    }
                }
            }
        }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.text = items[position].text
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is Header -> R.layout.layout_navigation_header
        else -> R.layout.layout_navigation
    }
}
