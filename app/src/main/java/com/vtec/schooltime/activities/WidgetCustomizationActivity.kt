package com.vtec.schooltime.activities

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.vtec.schooltime.*
import com.vtec.schooltime.databinding.WidgetCustomizationActivityBinding
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

@Serializable
class CustomField(var bgColor: Int?, var fgColor: Int?, var alpha: Float, var iconType: Int, var customMsg: String?)

typealias WidgetCustomization = MutableMap<Int, CustomField>

val fallbackWidgetCustomization: WidgetCustomization = mutableMapOf(
    R.string.before_subject to CustomField(null, null, 0.7f, R.string.widget_weather_icon, null),
    R.string.during_subject to CustomField(null, null, 1f, R.string.widget_weather_icon, null),
    R.string.end_of_school to CustomField(null, null, 0.7f, R.string.widget_edit_icon, null),
    R.string.free_day to CustomField(null, null, 0f, R.string.widget_no_icon, null)
)

class WidgetCustomizationActivity : AppCompatActivity(), ColorPicker {
    override val colorPickerBinding get() = binding.colorPicker
    override val context = this as Context

    private lateinit var binding: WidgetCustomizationActivityBinding
    private var customization: WidgetCustomization = mutableMapOf()
    private val customFieldValues = mutableListOf(R.string.before_subject, R.string.during_subject, R.string.free_day)
    private val iconTypeValues = mutableListOf(R.string.widget_weather_icon, R.string.widget_edit_icon, R.string.widget_no_icon)
    private lateinit var colorTypeValues: MutableList<Int>

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.widget_custom_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onColorChange(color: Int) {
        if (colorTypeValues[binding.colorType.selectedItemPosition] == R.string.foreground) {
            customization[customFieldValues[binding.customField.selectedItemPosition]]?.fgColor = color
            setWidgetForegroundColor(color)
        } else {
            customization[customFieldValues[binding.customField.selectedItemPosition]]?.bgColor = color
            setWidgetBackgroundColor(color)
        }
    }

    private fun setWidgetAlpha(alpha: Float) {
        customization[customFieldValues[binding.customField.selectedItemPosition]]?.alpha = alpha
        binding.widget.bg.alpha = alpha
    }

    private fun setWidgetBackgroundColor(color: Int) {
        binding.widget.bg.setColorFilter(color)
    }

    private fun setWidgetForegroundColor(color: Int) {
        binding.widget.text.setTextColor(color)
        binding.widget.activityButton.setColorFilter(color)
        binding.widget.longName.setTextColor(color)
        binding.widget.shortName.setTextColor(color)

        binding.widget.startTime.setTextColor(color)
        binding.widget.endTime.setTextColor(color)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.save -> {
                try {
                    val file = File(getExternalFilesDir(null), "widget_customization.json")
                    Json.encodeToStream(customization, FileOutputStream(file))
                } catch (e: Exception) {
                    Toast.makeText(context, context.getString(R.string.save_fail), Toast.LENGTH_SHORT).show()
                    return false
                }
                MainActivity.didSchedulesUpdate.notify()
                Toast.makeText(context, context.getString(R.string.save_success), Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setWidgetIconType(iconType: Int) {
        customization[customFieldValues[binding.customField.selectedItemPosition]]?.iconType = iconType
        if (iconType == R.string.widget_no_icon) {
            binding.widget.activityButton.visibility = View.GONE
        } else {
            binding.widget.activityButton.visibility = View.VISIBLE
            if (iconType == R.string.widget_weather_icon)
                binding.widget.activityButton.setImageResource(if (context.isDarkMode) R.drawable.clear_sky_night_icon else R.drawable.clear_sky_day_icon)
            else
                binding.widget.activityButton.setImageResource(R.drawable.pen_icon)
        }
    }

    private fun showCustomFieldColorPickerColor(customization: CustomField, colorType: Int) {
        val color = if (colorType == R.string.foreground) customization.fgColor ?: context.getColor(R.color.app_fg) else
            customization.bgColor ?: context.getColor(R.color.app_bg)

        setHexColorEditText(color)
        setSlidersProgress(color)
    }

    private fun showCustomField(customFieldValue: Int) {
        customization[customFieldValue]?.let {
            binding.iconType.setSelection(iconTypeValues.indexOf(it.iconType))

            binding.alphaSlider.field = it.alpha
            setWidgetAlpha(it.alpha)

            when (customFieldValue) {
                R.string.before_subject -> {
                    colorTypeValues = mutableListOf(R.string.background, R.string.foreground)
                    val bgColor = it.bgColor ?: context.getColor(R.color.app_bg)
                    setWidgetBackgroundColor(bgColor)
                    val fgColor = it.fgColor ?: context.getColor(R.color.app_fg)
                    setWidgetForegroundColor(fgColor)

                    binding.widget.subject.visibility = View.VISIBLE
                    binding.widget.timeLine.visibility = View.GONE

                    var text: String? = null

                    val calendar = Calendar.getInstance()
                    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                    val currentTime = Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
                    val schedule = MainActivity.schedule
                    val schoolSubjects = MainActivity.subjects

                    val scheduleBlocks = schedule.getOrDefault(currentDayOfWeek, mutableListOf())
                    for (scheduleBlock in scheduleBlocks) {
                        val startDeltaTime = scheduleBlock.startTime - currentTime
                        if (startDeltaTime > 0) {
                            text = getBeforeSubjectText(context, startDeltaTime)
                            break
                        }
                    }

                    if (text == null)
                    {
                        text = getBeforeSubjectText(context, Time(0, 23))
                    }

                    val subject = MainActivity.subjects.toList()[0].second
                    binding.widget.shortName.text = subject.shortName
                    binding.widget.longName.text = subject.longName

                    binding.widget.text.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
                }
                R.string.during_subject -> {
                    colorTypeValues = mutableListOf()

                    var text: String? = null
                    var bgColor: Int? = null

                    val calendar = Calendar.getInstance()
                    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                    val currentTime = Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
                    val schedule = MainActivity.schedule
                    val schoolSubjects = MainActivity.subjects

                    val scheduleBlocks = schedule.getOrDefault(currentDayOfWeek, mutableListOf())
                    var currentScheduleBlock: ScheduleBlock? = null
                    var schoolSubject: SchoolSubject? = null
                    for (scheduleBlock in scheduleBlocks) {
                        schoolSubject = schoolSubjects[scheduleBlock.id] ?: continue
                        val startDeltaTime = scheduleBlock.startTime - currentTime
                        val endDeltaTime = scheduleBlock.endTime - currentTime
                        if (endDeltaTime > 0 && startDeltaTime <= 0) {
                            currentScheduleBlock = scheduleBlock
                            val result = getDuringSubjectTextAndColor(context, schoolSubject, endDeltaTime)
                            text = result.first
                            bgColor = result.second
                            break
                        }
                    }

                    if (currentScheduleBlock == null || schoolSubject == null || text == null || bgColor == null)
                    {
                        schoolSubject = MainActivity.subjects.toList()[0].second
                        currentScheduleBlock = ScheduleBlock(MainActivity.subjects.toList()[0].first, Time(8, 30), Time(1, 0))
                        val otherResult = getDuringSubjectTextAndColor(context, schoolSubject, currentScheduleBlock.endTime - Time(8, 40))
                        text = otherResult.first
                        bgColor = otherResult.second
                    }

                    binding.widget.text.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)

                    binding.widget.shortName.text = schoolSubject.shortName
                    binding.widget.longName.text = schoolSubject.longName

                    binding.widget.startTime.text = currentScheduleBlock.startTime.toString()
                    binding.widget.endTime.text = currentScheduleBlock.endTime.toString()

                    setWidgetBackgroundColor(bgColor)
                    val fgColor = getContrastingColor(bgColor)
                    setWidgetForegroundColor(fgColor)

                    binding.widget.subject.visibility = View.VISIBLE

                    binding.widget.timeLine.visibility = View.VISIBLE
                }
                R.string.end_of_school -> {
                    colorTypeValues = mutableListOf(R.string.background, R.string.foreground)

                    binding.widget.text.text = it.customMsg
                    setWidgetBackgroundColor(it.bgColor ?: context.getColor(R.color.app_bg))
                    setWidgetForegroundColor(it.fgColor ?: context.getColor(R.color.app_fg))

                    binding.widget.subject.visibility = View.GONE
                    binding.widget.timeLine.visibility = View.GONE
                }
                else -> {
                    colorTypeValues = mutableListOf(R.string.background)
                    val color = it.bgColor ?: context.getColor(R.color.app_bg)
                    setWidgetBackgroundColor(color)
                    setWidgetForegroundColor(getContrastingColor(color))

                    binding.widget.text.text = ""

                    binding.widget.subject.visibility = View.GONE
                    binding.widget.timeLine.visibility = View.GONE
                }
            }

            if (colorTypeValues.isEmpty()) {
                binding.colorPickerHeader.visibility = View.GONE
                binding.colorPicker.root.visibility = View.GONE
            } else {
                val colorTypeEntries = mutableListOf<String>()
                colorTypeValues.forEach { colorTypeEntries.add(context.getString(it)) }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colorTypeEntries)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.colorType.adapter = adapter

                showCustomFieldColorPickerColor(it, colorTypeValues[binding.colorType.selectedItemPosition])

                binding.colorPickerHeader.visibility = View.VISIBLE
                binding.colorPicker.root.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = WidgetCustomizationActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.widget)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            binding.dummyWall.setImageDrawable(WallpaperManager.getInstance(context).drawable)
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("end_of_school_msg", false))
            customFieldValues.add(R.string.end_of_school)

        fallbackWidgetCustomization[R.string.end_of_school]?.customMsg = Html.fromHtml(getString(R.string.fallback_end_of_school_msg), Html.FROM_HTML_MODE_COMPACT).toString()

        try {
            val file = File(getExternalFilesDir(null), "widget_customization.json")
            customization = Json.decodeFromStream(FileInputStream(file))
        } catch (ex: Exception) {
            Log.d("FileIO", "Widget customization not found or incorrect!")
        }

        if (customization.isEmpty())
            customization = fallbackWidgetCustomization


        run {
            val customFieldEntries = mutableListOf<String>()
            customFieldValues.forEach { customFieldEntries.add(context.getString(it)) }

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, customFieldEntries)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.customField.adapter = adapter

            binding.customField.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long ) {
                    showCustomField(customFieldValues[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }
        }

        run {
            val iconTypeEntries = mutableListOf<String>()
            iconTypeValues.forEach { iconTypeEntries.add(context.getString(it)) }

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, iconTypeEntries)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.iconType.adapter = adapter

            binding.iconType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long ) {
                    setWidgetIconType(iconTypeValues[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }
        }

        run {
            binding.colorType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long ) {
                    customization[customFieldValues[binding.customField.selectedItemPosition]]?.let {
                        showCustomFieldColorPickerColor(it, colorTypeValues[position])
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }
        }

        binding.fabRestore.setOnClickListener {
            val file = File(getExternalFilesDir(null), "widget_customization.json")
            Json.encodeToStream(fallbackWidgetCustomization, FileOutputStream(file))
            customization = Json.decodeFromStream(FileInputStream(file))

            showCustomField(customFieldValues[binding.customField.selectedItemPosition])
        }

        binding.alphaSlider.onSeekBarChangeListener = {
            setWidgetAlpha(it.field)
        }

        setupColorPickerUI()
    }
}