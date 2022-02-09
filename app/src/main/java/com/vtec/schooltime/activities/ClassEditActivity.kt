package com.vtec.schooltime.activities

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.vtec.schooltime.*
import com.vtec.schooltime.databinding.ClassEditActivityBinding

class ClassEditActivity : AppCompatActivity(), ColorPicker {
    override val colorPickerBinding get() = binding.colorPicker
    override val context = this as Context

    private lateinit var binding: ClassEditActivityBinding
    private var schoolClassId: String? = null
    private lateinit var schoolClassCard: ClassVH

    override fun onColorChange(color: Int) {
        schoolClassCard.color = color
    }

    override fun onBackPressed() {
        if (binding.classNameEdit.error.isNullOrEmpty())
        {
            val schoolClass = MainActivity.schoolClasses[schoolClassId]

            if (schoolClass != null && schoolClassId != schoolClassCard.name)
                MainActivity.schoolClasses.remove(schoolClassId)

            MainActivity.schoolClasses[schoolClassCard.name] = SchoolClass(schoolClassCard.color)
            MainActivity.didSchoolClassesUpdate.notify()
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

        binding = ClassEditActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.school_class)

        schoolClassId = intent.getStringExtra("school_class_id")
        if (schoolClassId != null)
            binding.classNameEdit.setText(schoolClassId)
        else
            binding.classNameEdit.error = getString(R.string.name_empty)

        val schoolClass = MainActivity.schoolClasses[schoolClassId]

        schoolClassCard = ClassVH(binding.displayCard)
        schoolClassCard.bind(Pair(schoolClassId, schoolClass), null, ClassVH.Mode.Display)

        val color = schoolClassCard.color
        setHexColorEditText(color)
        setSlidersProgress(color)

        binding.classNameEdit.doOnTextChanged { text, start, before, count ->
            schoolClassCard.name = text.toString()

            val schoolClassesWithSameName = MainActivity.schoolClasses.filter { x -> x.key == text.toString() && x.key != schoolClassId}

            if (text.isNullOrEmpty())
                binding.classNameEdit.error = getString(R.string.name_empty)
            else if (schoolClassesWithSameName.isNotEmpty())
                binding.classNameEdit.error = getString(R.string.name_exists)
            else
                binding.classNameEdit.error = null
        }

        setupColorPickerUI()
    }
}