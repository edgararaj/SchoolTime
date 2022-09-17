package com.vtec.schooltime

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.FileInputStream
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
        var schedule: SchoolSchedule = mutableMapOf()
        val lessons: SchoolLessons = mutableMapOf()
        var didLessonsUpdate: MutableLiveData<Boolean> = MutableLiveData(false)
        var didSchedulesUpdate: MutableLiveData<Boolean> = MutableLiveData(false)

        fun calculateSmallestScheduleBlockDelta(): Int
        {
            var result = 0
            schedule.forEach { (key, value) ->
                value.minByOrNull { scheduleBlock -> scheduleBlock.duration }?.duration?.averageHour?.let {
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
            val data = File(getExternalFilesDir(null), "data.hdvt").readText().split("|")

            schedule.clear()
            run {
                val init = Json.decodeFromString<SchoolSchedule>(data[0])
                init.forEach { (t, u) ->
                    if (schedule[t] == null) schedule[t] = mutableListOf()
                    u.forEach { schedule[t]?.add(it) }
                }
            }

            lessons.clear()
            run {
                val init = Json.decodeFromString<SchoolLessons>(data[1])
                init.forEach { (t, u) -> lessons[t] = u }
            }

        } catch (ex: Exception) {
            Log.d("FileIO", "App data not found or incorrect!")
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