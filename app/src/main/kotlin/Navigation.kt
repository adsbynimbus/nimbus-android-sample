package com.adsbynimbus.android.sample

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.*
import androidx.navigation.fragment.*
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.*
import com.adsbynimbus.android.sample.databinding.ActivityNavigationBinding
import com.adsbynimbus.android.sample.demand.*
import com.adsbynimbus.android.sample.dynamicadapter.GoogleAdMobDynamicFragment
import com.adsbynimbus.android.sample.mediation.DynamicPriceFragment
import com.adsbynimbus.android.sample.mediation.GoogleAdManagerYieldGroupFragment
import com.adsbynimbus.android.sample.rendering.AdManagerFragment
import com.adsbynimbus.android.sample.rendering.TestRenderFragment

val screens = mutableMapOf(
    "Main" to NavigationAdapter(items = arrayOf(
        "Show Ad Demo",
        "Mediation Platforms",
        "Third Party Demand",
        "Test Render",
        "Settings",
    )),
    "Show Ad Demo" to NavigationAdapter(destination = "Show Ad Demo", header = null, items = arrayOf(
        "Manually Rendered Ad",
        "Banner",
        "Banner With Refresh",
        "Inline Video",
        "Interstitial Hybrid",
        "Interstitial Static",
        "Interstitial Video",
        "Interstitial Video Without UI",
        "Rewarded Video",
        "Ads in ScrollView"
    )),
    "Mediation Platforms" to ConcatAdapter(
        NavigationAdapter(destination = "Google Mediation", header = "Google Ad Manager", items = arrayOf(
            "Banner",
            "Interstitial",
        )),
        NavigationAdapter(destination = "DynamicPriceRendering", header = "Dynamic Price", items = arrayOf(
            "Banner",
            "Inline Video",
            "Interstitial",
            "Rewarded Video",
            "Rewarded Interstitial",
            "Force Loss",
            "AdLoader Banner",
            "AdLoader Inline Video",
        )),
        NavigationAdapter(destination = "Dynamic Adapters for AdMob", header = "AdMob", items = arrayOf(
            "Dynamic Banner",
            "Dynamic Interstitial",
            "Dynamic Rewarded",
            "Dynamic Rewarded Interstitial",
        )),
),
    "Third Party Demand" to ConcatAdapter(
        NavigationAdapter(destination = "AdMobGDE", header = "AdMob", items = arrayOf(
            "Banner",
            "MREC",
            "Interstitial",
            "Rewarded",
            "Native",
        )),
        NavigationAdapter(destination = "APS", header = "APS", items = arrayOf(
            "APS Banner With Refresh",
            "APS Interstitial Hybrid",
        )),
        NavigationAdapter(destination = "Meta", header = "Meta Audience Network", items = arrayOf(
            "Meta Banner",
            "Meta Interstitial",
            "Meta Native",
            "Meta Rewarded Video",
        )),
        NavigationAdapter(destination = "Mintegral", header = "Mintegral", items = arrayOf(
            "Banner",
            "MREC",
            "Interstitial",
            "Rewarded",
            "Native",
        )),
        NavigationAdapter(destination = "MobileFuse", header = "MobileFuse", items = arrayOf(
            "Banner",
            "Banner With Refresh",
            "MREC",
            "Interstitial",
            "Rewarded",
        )),
        NavigationAdapter(destination = "Moloco", header = "Moloco", items = arrayOf(
            "Banner",
            "Interstitial",
            "Rewarded",
            "Native",
        )),
        NavigationAdapter(destination = "Unity", header = "Unity", items = arrayOf(
            "Banner",
            "Interstitial",
            "Rewarded Video",
        )),
        NavigationAdapter(destination = "Vungle", header = "Vungle", items = arrayOf(
            "Vungle Banner",
            "Vungle MREC",
            "Vungle Interstitial",
            "Vungle Rewarded",
            "Vungle Native Banner",
            "Vungle Native Video",
        )),
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
    fragment<GoogleAdManagerYieldGroupFragment>("Google Mediation/{item}") {
        label = context.getString(R.string.google_ad_manager)
        argument("item") { type = NavType.StringType }
    }
    fragment<DynamicPriceFragment>("DynamicPriceRendering/{item}") {
        argument("item") { type = NavType.StringType }
    }
    fragment<GoogleAdMobDynamicFragment>("Dynamic Adapters for AdMob/{item}") {
        label = context.getString(R.string.google_admob)
        argument("item") { type = NavType.StringType }
    }
    fragment<MetaFragment>("Meta/{item}") { argument("item") { type = NavType.StringType } }
    fragment<MobileFuseFragment>("MobileFuse/{item}")  { argument("item") { type = NavType.StringType } }
    fragment<AdmobFragment>("AdMobGDE/{item}") { argument("item") { type = NavType.StringType } }
    fragment<MintegralFragment>("Mintegral/{item}") { argument("item") { type = NavType.StringType } }
    fragment<MolocoFragment>("Moloco/{item}") { argument("item") { type = NavType.StringType } }
    fragment<UnityFragment>("Unity/{item}") { argument("item") { type = NavType.StringType } }
    fragment<VungleFragment>("Vungle/{item}") { argument("item") { type = NavType.StringType } }
}

var appGraph: NavController.() -> NavGraph = { createGraph(startDestination = "Main") { nimbusGraph(context) } }

class NavigationActivity : AppCompatActivity() {

    inline val inputMethodManager get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inline val title get() = getString(R.string.main_title)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityNavigationBinding.inflate(layoutInflater).also { setContentView(it.root) }.apply {
            with(supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment) {
                navController.graph = navController.appGraph()
                toolbar.setupWithNavController(navController)

                navController.addOnDestinationChangedListener { controller, dest, args ->
                    val isMainNavDestination = dest.route in arrayOf("Main", "Internal")
                    val isAdDemo = args?.containsKey("item") ?: false
                    headerTitle.apply {
                        text = when {
                            isMainNavDestination -> title
                            isAdDemo -> args?.getString("item")
                            else -> dest.route
                        }
                        gravity = if (isMainNavDestination) Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL else Gravity.BOTTOM
                        TextViewCompat.setTextAppearance(
                            this,
                            if (isMainNavDestination) R.style.Header_Main else R.style.Header_Title
                        )
                    }
                    headerSubtitle.apply {
                        text = dest.label ?: controller.previousBackStackEntry?.destination?.route
                        gravity = if (isMainNavDestination) Gravity.TOP or Gravity.CENTER_HORIZONTAL else Gravity.TOP
                    }

                    inputMethodManager.hideSoftInputFromWindow(root.windowToken, 0)
                    toolbar.title = null /* Clear title to prevent issues with UI */
                }
            }
        }
        PreferenceManager.getDefaultSharedPreferences(this).apply {
            registerOnSharedPreferenceChangeListener(SettingsListener)
            initNimbusFeatures()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(SettingsListener)
    }
}

class NavigationFragment : Fragment() {

    inline val navController get() = findNavController()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = RecyclerView(requireContext()).apply {
        adapter = screens[navController.currentDestination?.route]
        layoutManager = LinearLayoutManager(context)
        addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (view as? RecyclerView)?.swapAdapter(null, true)
    }
}

class TextViewHolder(val view: TextView) : RecyclerView.ViewHolder(view)

class NavigationAdapter(
    val destination: String? = null,
    val header: String? = null,
    val items: Array<String>,
) : RecyclerView.Adapter<TextViewHolder>() {

    val offset = if (header == null) 0 else 1

    class ViewHolder(val view: TextView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder =
        TextViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false) as TextView
    )

    override fun getItemCount(): Int = items.size + offset

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        val offsetPosition = position - offset
        holder.view.text = if (offsetPosition < 0) header else items[offsetPosition].also { item ->
            holder.view.setOnClickListener {
                it.findNavController().navigate(destination?.plus("/$item") ?: item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when {
        position - offset < 0 -> R.layout.layout_navigation_header
        else -> R.layout.layout_navigation
    }
}
