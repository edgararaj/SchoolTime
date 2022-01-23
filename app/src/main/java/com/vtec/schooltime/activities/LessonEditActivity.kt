package com.vtec.schooltime.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.widget.doOnTextChanged
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.vtec.schooltime.*
import com.vtec.schooltime.databinding.LessonEditActivityBinding

class LessonEditActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {
    private lateinit var binding: LessonEditActivityBinding
    private var baseSchoolLessonId: String? = null
    private lateinit var schoolLessonCard: LessonVH

    private fun setCardBackgroundColor(color: Int)
    {
        schoolLessonCard.color = color
    }

    private fun setHexColorEditText(color: Int)
    {
        binding.hexColorEdit.setText("%02X%02X%02X".format(Color.red(color), Color.green(color), Color.blue(color)))
    }

    private fun setSlidersProgress(color: Int)
    {
        binding.redSlider.progress = Color.red(color)
        binding.greenSlider.progress = Color.green(color)
        binding.blueSlider.progress = Color.blue(color)
    }

    private fun validateHex(text: CharSequence): Boolean
    {
        var validHex = true
        if (text.isNotEmpty())
        {
            for (char in text)
            {
                if (!char.isDigit() && char.uppercaseChar() !in 'A'..'F')
                {
                    validHex = false
                    break
                }
            }
        }
        binding.hexColorEdit.error = if (validHex) null else getString(R.string.invalid_hex)
        return validHex
    }

    override fun onBackPressed() {
        if (binding.shortLessonNameEdit.error.isNullOrEmpty() && binding.longLessonNameEdit.error.isNullOrEmpty())
             {
                val newSchoolLessonId = binding.shortLessonNameEdit.text.toString()
                val schoolLesson = MainActivity.lessons[baseSchoolLessonId]
                if (schoolLesson == null || newSchoolLessonId != baseSchoolLessonId)
                {
                    MainActivity.lessons[newSchoolLessonId] = SchoolLesson(newSchoolLessonId, schoolLessonCard.longName, schoolLessonCard.color)
                    if (newSchoolLessonId != baseSchoolLessonId)
                        MainActivity.lessons.remove(baseSchoolLessonId)

                    MainActivity.didLessonsUpdate.notify()
                }
                else
                {
                    schoolLesson.apply {
                        longName = schoolLessonCard.longName
                        color = schoolLessonCard.color
                    }
                }
            }
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

        binding = LessonEditActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        baseSchoolLessonId = intent.getStringExtra("school_lesson_id")
        if (baseSchoolLessonId != null)
            binding.shortLessonNameEdit.setText(baseSchoolLessonId)
        else
            binding.shortLessonNameEdit.error = getString(R.string.name_empty)

        val schoolLesson = MainActivity.lessons[baseSchoolLessonId]

        schoolLessonCard = LessonVH(binding.displayCard)
        schoolLessonCard.bind(schoolLesson, null, LessonVH.Mode.Display)
        binding.longLessonNameEdit.setText(schoolLessonCard.longName)

        val color = schoolLesson?.color ?: Color.BLACK
        setHexColorEditText(color)
        setSlidersProgress(color)

        binding.colorWheel.setOnClickListener {
            ColorPickerDialog.Builder(this)
                .setTitle(getString(R.string.color_wheel))
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(getString(R.string.select),
                    object : ColorEnvelopeListener {
                        override fun onColorSelected(envelope: ColorEnvelope, fromUser: Boolean) {
                            setCardBackgroundColor(envelope.color)
                            setHexColorEditText(envelope.color)
                            setSlidersProgress(envelope.color)
                        }
                    })
                .setNegativeButton(getString(R.string.cancel)) { dialogInterface, i -> dialogInterface.dismiss() }
                .attachAlphaSlideBar(false) // the default value is true.
                .attachBrightnessSlideBar(true) // the default value is true.
                .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                .show()
        }

        binding.hexColorEdit.doOnTextChanged { text, start, before, count ->
            if (text != null) validateHex(text)
        }

        binding.hexColorEdit.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(textView: TextView, actionId: Int, keyEvent: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || keyEvent?.action == KeyEvent.ACTION_DOWN && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (validateHex(textView.text))
                    {
                        val string = "#" + textView.text.toString().padEnd(6, '0')
                        val color = Color.parseColor(string)
                        setCardBackgroundColor(color)
                        setHexColorEditText(color)
                        setSlidersProgress(color)

                        // occult keyboard
                        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.hideSoftInputFromWindow(binding.hexColorEdit.windowToken, 0)

                        return true
                    }
                }
                return false
            }
        })

        binding.longLessonNameEdit.doOnTextChanged { text, start, before, count ->
            schoolLessonCard.longName = text.toString()
            if (text.isNullOrEmpty())
                binding.longLessonNameEdit.error = getString(R.string.name_empty)
            else
                binding.longLessonNameEdit.error = null
        }

        binding.shortLessonNameEdit.doOnTextChanged { text, start, before, count ->
            val schoolLesson = MainActivity.lessons[text.toString()]
            if (text.isNullOrEmpty())
                binding.shortLessonNameEdit.error = getString(R.string.name_empty)
            else if (schoolLesson != null && text.toString() != baseSchoolLessonId)
                binding.shortLessonNameEdit.error = getString(R.string.name_exists)
            else
                binding.shortLessonNameEdit.error = null
        }

        binding.redSlider.setOnSeekBarChangeListener(this)
        binding.greenSlider.setOnSeekBarChangeListener(this)
        binding.blueSlider.setOnSeekBarChangeListener(this)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (!fromUser) return
        var color = 0
        when (seekBar.id)
        {
            R.id.red_slider -> color = Color.rgb(progress, binding.greenSlider.progress, binding.blueSlider.progress)
            R.id.green_slider -> color = Color.rgb(binding.redSlider.progress, progress, binding.blueSlider.progress)
            R.id.blue_slider -> color = Color.rgb(binding.redSlider.progress, binding.greenSlider.progress, progress)
        }
        setCardBackgroundColor(color)
        setHexColorEditText(color)
    }

    override fun onStartTrackingTouch(p0: SeekBar?) { }
    override fun onStopTrackingTouch(p0: SeekBar?) { }
}