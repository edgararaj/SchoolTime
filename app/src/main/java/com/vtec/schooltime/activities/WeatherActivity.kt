package com.vtec.schooltime.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.vtec.schooltime.R
import com.vtec.schooltime.databinding.ActivitySecondaryBinding

class WeatherActivity: AppCompatActivity() {
    private lateinit var binding: ActivitySecondaryBinding

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId)
        {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySecondaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val graph = navController.navInflater.inflate(R.navigation.secondary_navigation)
        graph.startDestination = R.id.nav_weather_location
        navController.graph = graph
    }
}