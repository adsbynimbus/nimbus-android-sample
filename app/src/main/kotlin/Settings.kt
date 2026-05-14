package com.adsbynimbus.android.sample

import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.PreferenceFragmentCompat
import com.adsbynimbus.Nimbus
import com.adsbynimbus.rtb.UID

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
                    Nimbus.configuration.identity.add(
                        source = "tradedesk.com", ids = setOf(UID(id = "TestUID2Token"))
                    )
                } else disableTradedeskId()
            }
            "coppa_on" -> Nimbus.configuration.coppa = getBoolean(it, false)
            "user_did_consent" -> getBoolean(it, false).let { consent ->
                Nimbus.IAB.tcfString = if (consent) tcfString else null
            }
            "ccpa_consent" -> getBoolean(it, false).let { enabled ->
                Nimbus.IAB.usPrivacyString = "1NYN".takeIf { enabled }
            }
            "enabled_gpp" -> getBoolean(it, false).let { testGppEnabled ->
                if (testGppEnabled) {
                    Nimbus.IAB.gppString = gppTestString
                    Nimbus.IAB.gppSID = gppTestSids
                } else {
                    Nimbus.IAB.gppString = null
                    Nimbus.IAB.gppSID = null
                }
            }
        }
    }
}

fun disableTradedeskId() {
    Nimbus.configuration.identity.clear("tradedesk.com")
}

val SharedPreferences.forceAdRequestError get() = getBoolean("force_no_fill", false)
