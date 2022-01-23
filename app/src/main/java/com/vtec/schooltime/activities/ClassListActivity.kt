package com.vtec.schooltime.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.vtec.schooltime.R
import com.vtec.schooltime.databinding.ActivitySecondaryBinding

class ClassListActivity: AppCompatActivity() {
    private lateinit var binding: ActivitySecondaryBinding

    class Contract : ActivityResultContract<Unit, String>() {
        override fun createIntent(context: Context, input: Unit?) = Intent(context, ClassListActivity::class.java)

        override fun parseResult(resultCode: Int, intent: Intent?) = intent?.getStringExtra("school_class_id")
    }

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
        graph.startDestination = R.id.nav_class_list
        navController.graph = graph
    }
}