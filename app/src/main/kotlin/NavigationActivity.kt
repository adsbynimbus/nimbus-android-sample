package com.adsbynimbus.android.sample

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.Gravity.BOTTOM
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.Gravity.TOP
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import androidx.core.widget.TextViewCompat.setTextAppearance
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.adsbynimbus.android.sample.databinding.ActivityNavigationBinding
import com.adsbynimbus.android.sample.databinding.CustomDialogBinding

// This variable is top level to avoid creating a companion object on NavigationActivity

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
                        gravity = if (isMainNavDestination) BOTTOM or CENTER_HORIZONTAL else BOTTOM
                        setTextAppearance(this, if (isMainNavDestination) R.style.MainTitle else R.style.Title)
                    }
                    headerSubtitle.apply {
                        text = dest.label ?: controller.previousBackStackEntry?.destination?.route
                        gravity = if (isMainNavDestination) TOP or CENTER_HORIZONTAL else TOP
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

fun Context.showCustomDialog(message: String): AlertDialog = AlertDialog.Builder(this)
    .setCancelable(false)
    .create().apply {
        setView(CustomDialogBinding.inflate(LayoutInflater.from(this@showCustomDialog)).apply {
            description.text = getString(R.string.custom_dialog_message, message)
            button.setOnClickListener { dismiss() }
        }.root)
    }

