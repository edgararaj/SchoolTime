package com.vtec.schooltime.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.vtec.schooltime.databinding.ActivitySecondaryBinding

class LessonListActivity: AppCompatActivity() {
    private lateinit var binding: ActivitySecondaryBinding

    class Contract : ActivityResultContract<Unit, String>() {
        override fun createIntent(context: Context, input: Unit?) = Intent(context, LessonListActivity::class.java)

        override fun parseResult(resultCode: Int, intent: Intent?) = intent?.getStringExtra("school_lesson_id")
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
    }
}