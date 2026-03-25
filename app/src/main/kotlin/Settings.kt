package com.adsbynimbus.android.sample

import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.PreferenceFragmentCompat
import com.adsbynimbus.Nimbus
import com.adsbynimbus.internal.addOrReplace
import com.adsbynimbus.internal.eid
import com.adsbynimbus.request.USPrivacyString

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}

object SettingsListener : SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        key?.let { sharedPreferences.initNimbusFeatures(setOf(key)) }
    }
}

const val gppTestString =
    "DBABMA~CLcVDxRMWfGmWAVAHCENAXCkAKDAADnAABRgA5mdfCKZuYJez-NQm0TBMYA4oCAAGQYIAAAAAAEAIAEgAA.argAC0gAAAAAAAAAAAA"
const val gppTestSids = "2"
const val tcfString =
    "CLcVDxRMWfGmWAVAHCENAXCkAKDAADnAABRgA5mdfCKZuYJez-NQm0TBMYA4oCAAGQYIAAAAAAEAIAEgAA.argAC0gAAAAAAAAAAAA"

fun SharedPreferences.setGppInSharedPrefs(enabled: Boolean) = edit {
    if (enabled) putString("IABGPP_HDR_GppString", gppTestString) else remove("IABGPP_HDR_GppString")
    if (enabled) putString("IABGPP_GppSID", gppTestSids) else remove("IABGPP_GppSID")
}


fun SharedPreferences.initNimbusFeatures(features: Set<String> = all.keys) {
    features.forEach {
        when (it) {
            "test_mode" -> getBoolean(it, false).let { enabled ->
                Nimbus.configuration.testMode = enabled
                if (!enabled) {
                    disableTradedeskId()
                    edit { putBoolean("send_tradedesk_id", false) }
                }
            }
            "send_tradedesk_id" -> getBoolean(it, false).let { enabled ->
                if (enabled && Nimbus.configuration.testMode) {
                    Nimbus.addOrReplace(eid(source = "tradedesk.com", ids = setOf("TestUID2Token")))
                } else disableTradedeskId()
            }
            "coppa_on" -> Nimbus.configuration.coppa = getBoolean(it, false)
            "user_did_consent" -> getBoolean(it, false).let { consent ->
                edit { if (consent) putString("IABTCF_TCString", tcfString) else remove("IABTCF_TCString") }
            }
            "ccpa_consent" -> getBoolean(it, false).let { enabled ->
                USPrivacyString = "1NYN".takeIf { enabled }
            }
            "enabled_gpp" -> setGppInSharedPrefs(enabled = getBoolean(it, false))
        }
    }
}

fun disableTradedeskId() {
    Nimbus.configuration.extendedIds.removeAll { it.source == "tradedesk.com" }
}

val SharedPreferences.forceAdRequestError get() = getBoolean("force_no_fill", false)
