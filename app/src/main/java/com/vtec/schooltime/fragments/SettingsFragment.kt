package com.vtec.schooltime.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.vtec.schooltime.R
import com.vtec.schooltime.activities.WeatherActivity
import com.vtec.schooltime.activities.WidgetCustomizationActivity

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findPreference<Preference>("weather_location")?.setOnPreferenceClickListener {
            startActivity(Intent(context, WeatherActivity::class.java))
            true
        }

        findPreference<Preference>("widget_customization")?.setOnPreferenceClickListener {
            val niceContext = context ?: return@setOnPreferenceClickListener true
            val niceActivity = activity ?: return@setOnPreferenceClickListener true
            if (ActivityCompat.checkSelfPermission(niceContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(niceActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
            }

            startActivity(Intent(context, WidgetCustomizationActivity::class.java))
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}