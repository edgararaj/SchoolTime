package com.vtec.schooltime.activities

import android.os.Bundle
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
        MainActivity.schoolLessons?.value = MainActivity.schoolLessons?.value
        MainActivity.schedule?.value = MainActivity.schedule?.value
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
        val schoolLessonListSelector = registerForActivityResult(LessonListActivity.Contract()) { schoolLessonId ->
            if (schoolLessonId != null)
            {
                binding.dayOfWeekCard.scheduleBlocks.visibility = View.VISIBLE
                MainActivity.schedule?.mutation {
                    it.value?.get(dayOfWeek)?.add(ScheduleBlock(schoolLessonId, Time(0, 0), Time(0, 0)))
                }
            }
        }

        val dayOfWeekCard = DayOfWeekVH(binding.dayOfWeekCard)
        val schedule = MainActivity.schedule?.value?.get(dayOfWeek)

        val icon = AppCompatResources.getDrawable(applicationContext, R.drawable.delete_icon)
        if (icon != null) {
            val action = { adapterPosition: Int ->
                MainActivity.schedule?.value?.get(dayOfWeek)?.let {
                    it.removeAt(adapterPosition)
                    if (it.size == 0)
                        binding.dayOfWeekCard.scheduleBlocks.visibility = View.GONE
                }
                binding.dayOfWeekCard.scheduleBlocks.adapter?.notifyItemRemoved(adapterPosition)
                Unit
            }
//            val onMove = { recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder ->
//                if (schedule != null)
//                {
//                    Collections.swap(schedule, viewHolder.adapterPosition, target.adapterPosition)
//                    binding.dayOfWeekCard.scheduleBlocks.adapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
//                }
//                true
//            }
            val itemTouchHelper = ItemTouchHelper(ItemSlideAction(this, icon, true, action, null))
            itemTouchHelper.attachToRecyclerView(binding.dayOfWeekCard.scheduleBlocks)
            if (schedule != null)
            {
                dayOfWeekCard.bind(schedule, dayOfWeek, schoolLessonListSelector)
            }
        }

        val observer = Observer<Any> {
            val schedule = MainActivity.schedule?.value?.get(dayOfWeek)
            if (schedule != null)
                dayOfWeekCard.bind(schedule, dayOfWeek, schoolLessonListSelector)
        }

        MainActivity.schoolLessons?.observe(this, observer)
        MainActivity.schedule?.observe(this, observer)
    }
}