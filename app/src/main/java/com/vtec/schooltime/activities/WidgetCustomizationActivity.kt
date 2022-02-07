package com.vtec.schooltime.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.preference.PreferenceManager
import com.vtec.schooltime.*
import com.vtec.schooltime.databinding.ClassEditActivityBinding
import com.vtec.schooltime.databinding.WidgetCustomizationActivityBinding
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

enum class WidgetIcon {
    None, Edit, Weather
}

@Serializable
class CustomField(var bgColor: Int?, var fgColor: Int?, var alpha: Float, var iconType: WidgetIcon)

typealias WidgetCustomization = MutableMap<Int, CustomField>

val fallbackWidgetCustomization: WidgetCustomization = mutableMapOf(
    R.string.before_lesson to CustomField(null, null, 0.3f, WidgetIcon.Weather),
    R.string.during_lesson to CustomField(null, null, 1f, WidgetIcon.Weather),
    R.string.end_of_school to CustomField(null, null, 0.3f, WidgetIcon.Edit),
    R.string.free_day to CustomField(null, null, 0.3f, WidgetIcon.None))

class WidgetCustomizationActivity : AppCompatActivity(), ColorPicker {
    override val colorPickerBinding get() = binding.colorPicker
    override val context = this as Context

    private lateinit var binding: WidgetCustomizationActivityBinding
    private var customization: WidgetCustomization = mutableMapOf()
    private val customFieldValues = mutableListOf(R.string.before_lesson, R.string.during_lesson, R.string.free_day)

    override fun setCardBackgroundColor(color: Int)
    {
        customization[customFieldValues[binding.customField.selectedItemPosition]]?.bgColor = color
        binding.widget.bg.setColorFilter(color)
        val contrastyFgColor = getContrastingColor(color)
        binding.widget.text.setTextColor(contrastyFgColor)
        binding.widget.activityButton.setColorFilter(contrastyFgColor)
    }

    override fun onBackPressed() {
        val file = File(getExternalFilesDir(null), "widget_customization.json")
        Json.encodeToStream(customization, FileOutputStream(file))
        super.onBackPressed()
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

        binding = WidgetCustomizationActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.widget)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("end_of_school", false))
            customFieldValues.add(R.string.end_of_school)

        try {
            val file = File(getExternalFilesDir(null), "widget_customization.json")
            customization = Json.decodeFromStream(FileInputStream(file))
        } catch (ex: Exception) {
            Log.d("FileIO", "Widget customization not found or incorrect!")
        }

        if (customization.isEmpty())
            customization = fallbackWidgetCustomization

        val customFieldEntries = mutableListOf<String>()
        customFieldValues.forEach { customFieldEntries.add(context.getString(it)) }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, customFieldEntries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.customField.adapter = adapter

        binding.customField.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected( parent: AdapterView<*>?, view: View?, position: Int, id: Long ) {
                customization[customFieldValues[position]]?.let {
                    val color = it.bgColor ?: context.getColor(R.color.app_bg)
                    setCardBackgroundColor(color)
                    setHexColorEditText(color)
                    setSlidersProgress(color)
                }

                when (customFieldValues[position])
                {
                    R.string.before_lesson -> {
                        val text = getBeforeLessonText(context, MainActivity.lessons.toList()[0].second, Time(0, 23))
                        binding.widget.text.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
                    }
                    R.string.during_lesson -> {
                        val (text, bgColor) = getDuringLessonTextAndColor(context, MainActivity.lessons.toList()[0].second, Time(0, 23))
                        binding.widget.text.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
                        setCardBackgroundColor(bgColor)
                        setHexColorEditText(bgColor)
                        setSlidersProgress(bgColor)
                    }
                    else -> {
                        binding.widget.text.text = ""
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.fabRestore.setOnClickListener {
            val file = File(getExternalFilesDir(null), "widget_customization.json")
            Json.encodeToStream(fallbackWidgetCustomization, FileOutputStream(file))
            customization = Json.decodeFromStream(FileInputStream(file))

            customization[customFieldValues[binding.customField.selectedItemPosition]]?.let {
                val color = it.bgColor ?: context.getColor(R.color.app_bg)
                setCardBackgroundColor(color)
                setHexColorEditText(color)
                setSlidersProgress(color)
            }
        }

        setupColorPickerUI()
    }
}