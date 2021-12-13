package com.vtec.schooltime.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.vtec.schooltime.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}