package com.adsbynimbus.android.sample

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.adsbynimbus.Nimbus
import com.adsbynimbus.ViewabilityProvider
import com.adsbynimbus.openrtb.request.User
import com.adsbynimbus.request.RequestManager

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}

object SettingsListener : SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        sharedPreferences.initNimbusFeatures(setOf(key))
    }
}

fun SharedPreferences.initNimbusFeatures(features: Set<String> = all.keys) {
    features.forEach {
        when (it) {
            "test_mode" -> Nimbus.setTestMode(getBoolean(it, true))
            "coppa_on" -> Nimbus.setCOPPA(getBoolean(it, false))
            "user_did_consent" -> getBoolean(it, false).let { consent ->
                if (!consent) RequestManager.getUser()?.ext?.did_consent = 0 else {
                    RequestManager.setUser((RequestManager.getUser() ?: User()).apply {
                        ext = (ext ?: User.Extension()).apply { did_consent = 1 }
                    })
                }
            }
            "enable_viewability" ->
                ViewabilityProvider.thirdPartyViewabilityEnabled = getBoolean(it, false)
        }
    }
}

val SharedPreferences.forceAdRequestError get() = getBoolean("force_no_fill", false)
