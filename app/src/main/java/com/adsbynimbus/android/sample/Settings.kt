package com.adsbynimbus.android.sample

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.adsbynimbus.Nimbus
import com.adsbynimbus.ViewabilityProvider

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
                edit().apply {
                    if (consent) putString("IABTCF_TCString", tcfString) else remove("IABTCF_TCString")
                }.apply()
            }
            "ccpa_consent" -> Nimbus.usPrivacyString = "1NYN".takeIf {_ -> getBoolean(it, false) }
            "enable_viewability" -> ViewabilityProvider.thirdPartyViewabilityEnabled = getBoolean(it, false)
        }
    }
}

val SharedPreferences.forceAdRequestError get() = getBoolean("force_no_fill", false)
