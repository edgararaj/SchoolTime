package com.vtec.schooltime

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
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
import kotlinx.serialization.encodeToString
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
        var schedule: SchoolSchedule = mutableMapOf(Calendar.MONDAY to mutableListOf(), Calendar.TUESDAY to mutableListOf(), Calendar.WEDNESDAY to mutableListOf(), Calendar.THURSDAY to mutableListOf(), Calendar.FRIDAY to mutableListOf(), Calendar.SATURDAY to mutableListOf(), Calendar.SUNDAY to mutableListOf())
        var subjects: SchoolSubjects = mutableMapOf()
        var didSubjectsUpdate: MutableLiveData<Boolean> = MutableLiveData(false)
        var didSchedulesUpdate: MutableLiveData<Boolean> = MutableLiveData(false)

        fun calculateSmallestScheduleBlockDelta(): Int
        {
            var result: Int? = null
            schedule.forEach { (key, value) ->
                value.minByOrNull { scheduleBlock -> scheduleBlock.duration }?.duration?.averageHour?.let {
                    if (result == null)
                        result = it
                    else if (it < result!!)
                        result = it
                }
            }
            return result ?: 0
        }
    }

    private fun loadHDVT(string: String): Boolean
    {
        val data = string.split("|")
        try {
            schedule.clear()
            run {
                val init = Json.decodeFromString<SchoolSchedule>(data[0])
                for (i in 1..7)
                {
                    if (init.containsKey(i))
                    {
                        if (schedule[i] == null) schedule[i] = init[i]?.toMutableList()!!
                    }
                    else
                    {
                        schedule[i] = mutableListOf()
                    }
                }
            }
            subjects.clear()
            run {
                Json.decodeFromString<SchoolSubjects>(data[1]).forEach { (s, schoolSubject) ->
                    subjects[s] = schoolSubject
                }
            }
        }
        catch (e: java.lang.Exception)
        {
            return false
        }

        schedule.forEach { entry ->
            entry.value.sortBy { it.startTime }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data?.data == null) return
        val contentResolver = binding.root.context.contentResolver
        data.data?.let { uri ->
            if (requestCode == 12) {
                // Import HDVT
                val data = readTextFromUri(uri, contentResolver)
                if (!loadHDVT(data)) {
                    Toast.makeText(applicationContext, "File is not HDVT!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    didSchedulesUpdate.notify()
                    didSubjectsUpdate.notify()
                    Toast.makeText(applicationContext, "HDVT File Loaded", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            else if (requestCode == 13) {
                // Export HDVT
                alterDocument(
                    uri,
                    Json.encodeToString(schedule) + "|" + Json.encodeToString(subjects),
                    contentResolver
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId)
        {
            R.id.import_export_hdvt -> {
                val popup = PopupMenu(this, findViewById(R.id.import_export_hdvt))
                popup.inflate(R.menu.import_export_menu)
                popup.setOnMenuItemClickListener {
                    when (it.itemId)
                    {
                        R.id.import_hdvt -> {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                            }

                            startActivityForResult(intent, 12)
                        }
                        R.id.export_hdvt -> {
                            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                                putExtra(Intent.EXTRA_TITLE, "data.hdvt")
                            }
                            startActivityForResult(intent, 13)
                        }
                    }
                    true
                }
                popup.show()
            }
            R.id.save_hdvt -> {
                try {
                    val subjectsFile = File(applicationContext.getExternalFilesDir(null), "data.hdvt")
                    subjectsFile.writeText(Json.encodeToString(schedule) + "|" + Json.encodeToString(subjects))
                }
                catch (e: Exception)
                {
                    Toast.makeText(applicationContext, applicationContext.getString(R.string.save_fail), Toast.LENGTH_SHORT).show()
                    return false
                }

                Toast.makeText(applicationContext, applicationContext.getString(R.string.save_success), Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val internalData = File(getExternalFilesDir(null), "data.hdvt")
        if (internalData.exists())
        {
            if (!loadHDVT(internalData.readText()))
            {
                Log.d("FileIO", "App data not found or incorrect!")
            }
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