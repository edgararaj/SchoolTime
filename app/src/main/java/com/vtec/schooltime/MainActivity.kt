package com.vtec.schooltime

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.marginTop
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.vtec.schooltime.databinding.ActivityMainBinding
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*

class ItemDecoration(private val columns: Int): RecyclerView.ItemDecoration()
{
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)

        val margin = parent.resources.getDimension(R.dimen.screen_bottom_margin).toInt()

        parent.adapter?.let {
            if ((position + 1) % columns == 0) outRect.right = parent.children.first().marginTop
            if (columns == 1)
                if (position >= it.itemCount - columns) outRect.bottom = margin
        }
    }
}

data class LocationEntry(val name: String, val country: String, val lat: Double, val lon: Double)

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    companion object {
        var weatherLocation: MutableLiveData<LocationEntry> = MutableLiveData(LocationEntry("Braga", "PT", 41.5510583, -8.4280045))
        var schoolClasses: SchoolClasses = mutableMapOf()
        var schedule: SchoolSchedule = mutableMapOf()
        val lessons: SchoolLessons = mutableMapOf()
        var didLessonsUpdate: MutableLiveData<Boolean> = MutableLiveData(false)
        var didSchedulesUpdate: MutableLiveData<Boolean> = MutableLiveData(false)
        var didSchoolClassesUpdate: MutableLiveData<Boolean> = MutableLiveData(false)

        fun calculateSmallestScheduleBlockDelta(): Int
        {
            var result = 0
            schedule.forEach { (key, value) ->
                value.minByOrNull { scheduleBlock -> scheduleBlock.delta }?.delta?.averageHour?.let {
                    if (key == Calendar.MONDAY)
                        result = it
                    else if (it < result)
                        result = it
                }
            }
            return result
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val file = File(getExternalFilesDir(null), "classes.json")
            schoolClasses = Json.decodeFromStream(FileInputStream(file))

            lessons.clear()
            run {
                val lessonsFile = File(getExternalFilesDir(null), "lessons.json")
                val init = Json.decodeFromStream<SchoolLessons>(FileInputStream(lessonsFile))
                init.forEach { (t, u) -> lessons[t] = u }
            }

            schedule.clear()
            run {
                val scheduleFile = File(getExternalFilesDir(null), "schedule.json")
                val init = Json.decodeFromStream<SchoolSchedule>(FileInputStream(scheduleFile))
                init.forEach { (t, u) ->
                    if (schedule[t] == null) schedule[t] = mutableListOf()
                    u.forEach { schedule[t]?.add(it) }
                }
            }
        } catch (ex: FileNotFoundException)
        {
            Log.d("FileIO", "App data not found!")
        }

        schedule.forEach { entry ->
            entry.value.sortBy { it.startTime }
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