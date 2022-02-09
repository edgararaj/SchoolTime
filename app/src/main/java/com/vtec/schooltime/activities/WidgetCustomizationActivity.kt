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
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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

@Serializable
class CustomField(var bgColor: Int?, var fgColor: Int?, var alpha: Float, var iconType: Int)

typealias WidgetCustomization = MutableMap<Int, CustomField>

val fallbackWidgetCustomization: WidgetCustomization = mutableMapOf(
    R.string.before_lesson to CustomField(null, null, 0.3f, R.string.widget_weather_icon),
    R.string.during_lesson to CustomField(null, null, 1f, R.string.widget_weather_icon),
    R.string.end_of_school to CustomField(null, null, 0.3f, R.string.widget_edit_icon),
    R.string.free_day to CustomField(null, null, 0.3f, R.string.widget_no_icon)
)

class WidgetCustomizationActivity : AppCompatActivity(), ColorPicker {
    override val colorPickerBinding get() = binding.colorPicker
    override val context = this as Context

    private lateinit var binding: WidgetCustomizationActivityBinding
    private var customization: WidgetCustomization = mutableMapOf()
    private val customFieldValues = mutableListOf(R.string.before_lesson, R.string.during_lesson, R.string.free_day)
    private val iconTypeValues = mutableListOf(R.string.widget_weather_icon, R.string.widget_edit_icon, R.string.widget_no_icon)
    private lateinit var colorTypeValues: MutableList<Int>

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
    }

    override fun onBackPressed() {
        val file = File(getExternalFilesDir(null), "widget_customization.json")
        Json.encodeToStream(customization, FileOutputStream(file))
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
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
                binding.widget.activityButton.setImageResource(R.drawable.widget_edit_icon)
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
                R.string.before_lesson -> {
                    colorTypeValues = mutableListOf(R.string.background, R.string.foreground)
                    setWidgetBackgroundColor(it.bgColor ?: context.getColor(R.color.app_bg))
                    setWidgetForegroundColor(it.fgColor ?: context.getColor(R.color.app_fg))

                    val text = getBeforeLessonText(context, MainActivity.lessons.toList()[0].second, Time(0, 23))
                    binding.widget.text.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
                }
                R.string.during_lesson -> {
                    colorTypeValues = mutableListOf()

                    val (text, bgColor) = getDuringLessonTextAndColor(context, MainActivity.lessons.toList()[0].second, Time(0, 23))
                    binding.widget.text.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
                    setWidgetBackgroundColor(bgColor)
                    setWidgetForegroundColor(getContrastingColor(bgColor))
                }
                R.string.end_of_school -> {
                    colorTypeValues = mutableListOf(R.string.background, R.string.foreground)
                    setWidgetBackgroundColor(it.bgColor ?: context.getColor(R.color.app_bg))
                    setWidgetForegroundColor(it.fgColor ?: context.getColor(R.color.app_fg))
                }
                else -> {
                    colorTypeValues = mutableListOf(R.string.background)
                    val color = it.bgColor ?: context.getColor(R.color.app_bg)
                    setWidgetBackgroundColor(color)
                    setWidgetForegroundColor(getContrastingColor(color))

                    binding.widget.text.text = ""
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

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getBoolean("end_of_school_msg", false))
            customFieldValues.add(R.string.end_of_school)

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