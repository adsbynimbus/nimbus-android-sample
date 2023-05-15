package com.adsbynimbus.android.sample

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.adsbynimbus.android.sample.databinding.ActivityNavigationBinding

// This variable is top level to avoid creating a companion object on NavigationActivity

class NavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityNavigationBinding.inflate(layoutInflater).also { setContentView(it.root) }.apply {
            with(supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment) {
                navController.graph = navController.nimbusGraph()
                toolbar.setupWithNavController(navController)
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).let {
                    navController.addOnDestinationChangedListener { _, _, _ ->
                        it.hideSoftInputFromWindow(root.windowToken, 0)
                    }
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
