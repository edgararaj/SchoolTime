package com.vtec.schooltime.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.vtec.schooltime.*
import com.vtec.schooltime.databinding.SubjectEditActivityBinding

class SubjectEditActivity : AppCompatActivity(), ColorPicker {
    override val colorPickerBinding get() = binding.colorPicker
    override val context = this as Context

    private lateinit var binding: SubjectEditActivityBinding
    private var baseSchoolSubjectId: String? = null
    private lateinit var schoolSubjectCard: SubjectVH

    override fun onColorChange(color: Int) {
        schoolSubjectCard.color = color
    }

    override fun onBackPressed() {
        if (binding.shortSubjectNameEdit.error.isNullOrEmpty() && binding.longSubjectNameEdit.error.isNullOrEmpty())
        {
            val newSchoolSubjectId = binding.shortSubjectNameEdit.text.toString()
            val schoolSubject = MainActivity.subjects[baseSchoolSubjectId]
            if (newSchoolSubjectId != baseSchoolSubjectId)
            {
                MainActivity.subjects[newSchoolSubjectId] = SchoolSubject(newSchoolSubjectId, schoolSubjectCard.longName, schoolSubjectCard.color)
                if (newSchoolSubjectId != baseSchoolSubjectId)
                    MainActivity.subjects.remove(baseSchoolSubjectId)

                MainActivity.schedule.forEach { (i, mutableList) ->
                    mutableList.forEach {
                        if (it.id == baseSchoolSubjectId)
                            it.id = newSchoolSubjectId
                    }
                }
            }
            else
            {
                schoolSubject?.apply {
                    longName = schoolSubjectCard.longName
                    color = schoolSubjectCard.color
                }
            }

            MainActivity.didSubjectsUpdate.notify()
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

        binding = SubjectEditActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = context.getString(R.string.school_subject)

        baseSchoolSubjectId = intent.getStringExtra("school_subject_id")
        if (baseSchoolSubjectId != null)
            binding.shortSubjectNameEdit.setText(baseSchoolSubjectId)
        else
            binding.shortSubjectNameEdit.error = getString(R.string.name_empty)

        val schoolSubject = MainActivity.subjects[baseSchoolSubjectId]

        schoolSubjectCard = SubjectVH(binding.displayCard)
        schoolSubjectCard.bind(schoolSubject, null, SubjectVH.Mode.Display)
        binding.longSubjectNameEdit.setText(schoolSubjectCard.longName)

        val color = schoolSubject?.color ?: Color.BLACK
        setHexColorEditText(color)
        setSlidersProgress(color)

        setupColorPickerUI()

        binding.longSubjectNameEdit.doOnTextChanged { text, start, before, count ->
            schoolSubjectCard.longName = text.toString()
            if (text.isNullOrEmpty())
                binding.longSubjectNameEdit.error = getString(R.string.name_empty)
            else
                binding.longSubjectNameEdit.error = null
        }

        binding.shortSubjectNameEdit.doOnTextChanged { text, start, before, count ->
            schoolSubjectCard.shortName = text.toString()
            val schoolSubject = MainActivity.subjects[text.toString()]
            if (text.isNullOrEmpty())
                binding.shortSubjectNameEdit.error = getString(R.string.name_empty)
            else if (schoolSubject != null && text.toString() != baseSchoolSubjectId)
                binding.shortSubjectNameEdit.error = getString(R.string.name_exists)
            else
                binding.shortSubjectNameEdit.error = null
        }
    }
}