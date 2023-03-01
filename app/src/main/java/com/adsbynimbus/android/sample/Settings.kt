package com.adsbynimbus.android.sample

import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.PreferenceFragmentCompat
import com.adsbynimbus.Nimbus

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

const val gppTestString =
    "DBABMA~CLcVDxRMWfGmWAVAHCENAXCkAKDAADnAABRgA5mdfCKZuYJez-NQm0TBMYA4oCAAGQYIAAAAAAEAIAEgAA.argAC0gAAAAAAAAAAAA"
const val gppTestSids = "2"

fun SharedPreferences.setGppInSharedPrefs(enabled: Boolean) = edit {
    if (enabled) putString("IABGPP_HDR_GppString", gppTestString) else remove("IABGPP_HDR_GppString")
    if (enabled) putString("IABGPP_GppSID", gppTestSids) else remove("IABGPP_GppSID")
}


fun SharedPreferences.initNimbusFeatures(features: Set<String> = all.keys) {
    features.forEach {
        when (it) {
            "test_mode" -> Nimbus.testMode = getBoolean(it, true)
            "coppa" -> Nimbus.COPPA = getBoolean(it, false)
            "user_did_consent" -> getBoolean(it, false).let { consent ->
                edit { if (consent) putString("IABTCF_TCString", tcfString) else remove("IABTCF_TCString") }
            }
            "ccpa_consent" -> Nimbus.usPrivacyString = "1NYN".takeIf { _ -> getBoolean(it, false) }
            "enable_viewability" -> Nimbus.thirdPartyViewabilityEnabled = getBoolean(it, true)
            "enabled_gpp" -> setGppInSharedPrefs(enabled = getBoolean(it, false))
        }
    }
}

val SharedPreferences.forceAdRequestError get() = getBoolean("force_no_fill", false)
