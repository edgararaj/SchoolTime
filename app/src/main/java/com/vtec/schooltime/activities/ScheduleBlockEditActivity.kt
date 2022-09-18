package com.vtec.schooltime.activities

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.vtec.schooltime.*
import com.vtec.schooltime.databinding.ScheduleBlockEditActivityBinding

class ScheduleBlockEditActivity : AppCompatActivity() {
    private lateinit var binding: ScheduleBlockEditActivityBinding
    private var dayOfWeek: Int = 0
    private var scheduleBlockPosition = 0
    private var scheduleBlockStartTime: Time = Time(0, 0)
        set(x)
        {
            binding.startTime.text = x.toString()
            field = x
        }

    private var scheduleBlockEndTime: Time = Time(0, 0)
        set(x)
        {
            binding.endTime.text = x.toString()
            field = x
        }

    private var scheduleBlockDuration: Time = Time(0, 0)
        set(x)
        {
            binding.duration.text = x.toString()
            field = x
        }
    private lateinit var schoolSubjectCard: SubjectVH
    private var lockDuration = false
    set(x)
    {
        binding.lock.setImageResource(if (x) R.drawable.lock_icon else R.drawable.lock_open_icon)
        field = x
    }

    override fun onBackPressed() {
        MainActivity.schedule[dayOfWeek]?.get(scheduleBlockPosition)?.apply {
            startTime = scheduleBlockStartTime
            duration = scheduleBlockEndTime - scheduleBlockStartTime
        }
        MainActivity.schedule[dayOfWeek]?.sortBy { it.startTime }
        MainActivity.didSchedulesUpdate.notify()
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
        supportActionBar?.title = getString(R.string.schedule_block)

        dayOfWeek = intent.getIntExtra("day_of_week", -1)
        scheduleBlockPosition = intent.getIntExtra("schedule_block_position", -1)
        val scheduleBlock = MainActivity.schedule[dayOfWeek]?.get(scheduleBlockPosition)
        val schoolSubject = MainActivity.subjects[scheduleBlock?.id]

        if (scheduleBlock == null || schoolSubject == null) return

        schoolSubjectCard = SubjectVH(binding.schoolSubjectCard)
        schoolSubjectCard.bind(schoolSubject, null, SubjectVH.Mode.Display)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        lockDuration = preferences.getBoolean("lock_duration", false)
        binding.lock.setOnClickListener {
            lockDuration = !lockDuration
        }

        scheduleBlockStartTime = scheduleBlock.startTime
        binding.startTime.setOnClickListener {
            val timePickerListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                scheduleBlockStartTime = Time(hour, minute)
                if (lockDuration || scheduleBlockStartTime > scheduleBlockEndTime)
                    scheduleBlockEndTime = scheduleBlockStartTime + scheduleBlockDuration
                else
                    scheduleBlockDuration = scheduleBlockEndTime - scheduleBlockStartTime
            }
            TimePickerDialog(this,
                R.style.ThemeOverlay_TimePicker, timePickerListener, scheduleBlockStartTime.hour, scheduleBlockStartTime.minute, true).show()
        }

        scheduleBlockEndTime = scheduleBlock.endTime
        binding.endTime.setOnClickListener {
            val timePickerListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                scheduleBlockEndTime = Time(hour, minute)
                if (lockDuration || scheduleBlockEndTime < scheduleBlockStartTime)
                    scheduleBlockStartTime = scheduleBlockEndTime - scheduleBlockDuration
                else
                    scheduleBlockDuration = scheduleBlockEndTime - scheduleBlockStartTime
            }
            TimePickerDialog(this,
                R.style.ThemeOverlay_TimePicker, timePickerListener, scheduleBlockEndTime.hour, scheduleBlockEndTime.minute, true).show()
        }

        scheduleBlockDuration = scheduleBlock.endTime - scheduleBlock.startTime
        binding.duration.setOnClickListener {
            val timePickerListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                scheduleBlockDuration = Time(hour, minute)
                scheduleBlockEndTime = scheduleBlockStartTime + scheduleBlockDuration
            }
            TimePickerDialog(this,
                R.style.ThemeOverlay_TimePicker, timePickerListener, scheduleBlockDuration.hour, scheduleBlockDuration.minute, true).show()
        }
    }
}