package com.vtec.schooltime.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.vtec.schooltime.*
import com.vtec.schooltime.databinding.LessonEditActivityBinding

class LessonEditActivity : AppCompatActivity(), ColorPicker {
    override val colorPickerBinding get() = binding.colorPicker
    override val context = this as Context

    private lateinit var binding: LessonEditActivityBinding
    private var baseSchoolLessonId: String? = null
    private lateinit var schoolLessonCard: LessonVH

    override fun setCardBackgroundColor(color: Int)
    {
        schoolLessonCard.color = color
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
            }
            else
            {
                schoolLesson.apply {
                    longName = schoolLessonCard.longName
                    color = schoolLessonCard.color
                }
            }

            MainActivity.didLessonsUpdate.notify()
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

        setupColorPickerUI()

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
    }
}