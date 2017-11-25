package com.example.android.sunshine

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat

/**
 * Created by p.s.curzytek on 11/24/2017.
 */
class SettingsFragment: PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val preference = findPreference(key)
        if (preference !is CheckBoxPreference) {
            setPreferenceSummary(preference, sharedPreferences.getString(key, ""))
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_screen)

        val preferences = preferenceScreen.sharedPreferences

        for (prefId in 0..(preferenceScreen.preferenceCount - 1)) {
            val pref = preferenceScreen.getPreference(prefId)
            if (pref !is CheckBoxPreference) {
                val value = preferences.getString(pref.key, "")
                setPreferenceSummary(pref, value)
            }
        }
    }

    private fun setPreferenceSummary(preference: Preference, value: String) {
        if (preference is ListPreference) {
            val prefIndex = preference.findIndexOfValue(value)
            if (prefIndex >= 0) {
                preference.summary = preference.entries[prefIndex]
            }
        } else {
            preference.summary = value
        }
    }

    override fun onStart() {
        preferenceManager.sharedPreferences
                .registerOnSharedPreferenceChangeListener(this)
        super.onStart()
    }

    override fun onDestroy() {
        preferenceManager.sharedPreferences
                .unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }
}

