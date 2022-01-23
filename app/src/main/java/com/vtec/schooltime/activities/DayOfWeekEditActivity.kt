package com.vtec.schooltime.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import com.vtec.schooltime.*
import com.vtec.schooltime.databinding.DayOfWeekEditActivityBinding

class DayOfWeekEditActivity: AppCompatActivity() {
    private lateinit var binding: DayOfWeekEditActivityBinding

    override fun onBackPressed() {
        MainActivity.didLessonsUpdate.notify()
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

        binding = DayOfWeekEditActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val dayOfWeek = intent.getIntExtra("day_of_week", -1)
        val schoolLessonListSelector = registerForActivityResult(LessonListActivity.Contract()) { (schoolLessonId, schoolClassId) ->
            if (schoolLessonId != null && schoolClassId != null)
            {
                binding.dayOfWeekCard.scheduleBlocks.visibility = View.VISIBLE
                MainActivity.schedule[dayOfWeek]?.let {
                    it.add(ScheduleBlock(schoolLessonId, Time(0, 0), Time(0, 0), schoolClassId))
                    it.sortBy { it.startTime }
                }
                MainActivity.didSchedulesUpdate.notify()
            }
        }

        val dayOfWeekCard = DayOfWeekVH(binding.dayOfWeekCard)
        val schedule = MainActivity.schedule[dayOfWeek]

        val icon = AppCompatResources.getDrawable(applicationContext, R.drawable.delete_icon)
        if (icon != null) {
            val action = { adapterPosition: Int ->
                MainActivity.schedule[dayOfWeek]?.let { schedule ->
                    schedule.removeAt(adapterPosition)
                    if (schedule.size == 0)
                        binding.dayOfWeekCard.scheduleBlocks.visibility = View.GONE
                }
                binding.dayOfWeekCard.scheduleBlocks.adapter?.notifyItemRemoved(adapterPosition)
                Unit
            }

            val itemTouchHelper = ItemTouchHelper(ItemSlideAction(this, icon, true, action, null))
            itemTouchHelper.attachToRecyclerView(binding.dayOfWeekCard.scheduleBlocks)
            if (schedule != null) dayOfWeekCard.bind(schedule, dayOfWeek, schoolLessonListSelector)
        }

        val observer = Observer<Any> {
            val schedule = MainActivity.schedule[dayOfWeek]
            if (schedule != null)
                dayOfWeekCard.bind(schedule, dayOfWeek, schoolLessonListSelector)
        }

        MainActivity.didLessonsUpdate.observe(this, observer)
        MainActivity.didSchedulesUpdate.observe(this, observer)
        MainActivity.didSchoolClassesUpdate.observe(this, observer)
    }
}