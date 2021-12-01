package com.vtec.schooltime

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.vtec.schooltime.databinding.ScheduleBlockEditActivityBinding

class ScheduleBlockEditActivity : AppCompatActivity() {
    private lateinit var binding: ScheduleBlockEditActivityBinding
    private var dayOfWeek: Int = 0
    private var scheduleBlockPosition = 0
    private lateinit var scheduleBlockStartTime: Time
    private lateinit var scheduleBlockEndTime: Time
    private lateinit var schoolClassCard: ClassVH

    override fun onBackPressed() {
        MainActivity.schedule?.mutation {
            it.value?.get(dayOfWeek)?.get(scheduleBlockPosition)?.apply {
                startTime = scheduleBlockStartTime
                endTime = scheduleBlockEndTime
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

        binding = ScheduleBlockEditActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dayOfWeek = intent.getIntExtra("day_of_week", -1)
        scheduleBlockPosition = intent.getIntExtra("schedule_block_position", -1)
        val scheduleBlock = MainActivity.schedule?.value?.get(dayOfWeek)?.get(scheduleBlockPosition)
        val schoolClass = MainActivity.schoolClasses?.value?.get(scheduleBlock?.schoolClassId)

        if (scheduleBlock == null || schoolClass == null) return

        schoolClassCard = ClassVH(binding.schoolClassCard)
        schoolClassCard.bind(schoolClass, null, ClassVH.Mode.Display)

        scheduleBlockStartTime = scheduleBlock.startTime
        binding.startTime.text = scheduleBlockStartTime.toString()
        binding.startTime.setOnClickListener {
            val timePickerListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                scheduleBlockStartTime = Time(hour, minute)
                binding.startTime.text = scheduleBlockStartTime.toString()
            }
            TimePickerDialog(this,
                R.style.ThemeOverlay_TimePicker, timePickerListener, scheduleBlock.startTime.hour, scheduleBlock.startTime.minute, true).show()
        }

        scheduleBlockEndTime = scheduleBlock.endTime
        binding.endTime.text = scheduleBlockEndTime.toString()
        binding.endTime.setOnClickListener {
            val timePickerListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                scheduleBlockEndTime = Time(hour, minute)
                binding.endTime.text = scheduleBlockEndTime.toString()
            }
            TimePickerDialog(this,
                R.style.ThemeOverlay_TimePicker, timePickerListener, scheduleBlock.endTime.hour, scheduleBlock.endTime.minute, true).show()
        }
    }
}