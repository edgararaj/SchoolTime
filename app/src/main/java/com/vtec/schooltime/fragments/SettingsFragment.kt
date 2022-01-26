package com.vtec.schooltime.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.vtec.schooltime.MainActivity
import com.vtec.schooltime.R
import com.vtec.schooltime.activities.WeatherActivity
import com.vtec.schooltime.notify

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findPreference<Preference>("weather_location")?.setOnPreferenceClickListener {
            startActivity(Intent(context, WeatherActivity::class.java))
            true
        }

        findPreference<SwitchPreference>("hide_weather")?.setOnPreferenceClickListener {
            MainActivity.weatherLocation.notify()
            true
        }

//        findPreference<SwitchPreference>("widget_customization")?.setOnPreferenceClickListener {
//            startActivity(Intent(context, WidgetActivity::class.java))
//            true
//        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}