package com.vtec.schooltime

import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.vtec.schooltime.databinding.ActivityMainBinding
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ItemDecoration(): RecyclerView.ItemDecoration()
{
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)

        val margin = parent.resources.getDimension(R.dimen.screen_bottom_margin).toInt()

        parent.adapter?.let { if (position == it.itemCount - 1) outRect.bottom = margin }
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    companion object {
        var schedule: Schedule? = null
        var schoolClasses: SchoolClasses? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        run {
            val scheduleFile = File(getExternalFilesDir(null), "schedule.json")
            val scheduleOutputStream = FileOutputStream(scheduleFile)
            Json.encodeToStream(fallbackSchedule, scheduleOutputStream)
            schedule = MutableLiveData(Json.decodeFromStream(FileInputStream(scheduleFile)))
        }

        run {
            val schoolClassesFile = File(getExternalFilesDir(null), "school_classes.json")
            val schoolClassesOutputStream = FileOutputStream(schoolClassesFile)
            Json.encodeToStream(fallbackSchoolClasses, schoolClassesOutputStream)
            schoolClasses = MutableLiveData(Json.decodeFromStream(FileInputStream(schoolClassesFile)))
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(binding.navView.menu, binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}