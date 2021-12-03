package com.vtec.schooltime

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.vtec.schooltime.databinding.DayOfWeekEditActivityBinding
import java.util.*

class DayOfWeekEditActivity: AppCompatActivity() {
    private lateinit var binding: DayOfWeekEditActivityBinding

    override fun onBackPressed() {
        MainActivity.schoolClasses?.value = MainActivity.schoolClasses?.value
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
        val schoolClassListSelector = registerForActivityResult(ClassListActivity.Contract()) { schoolClassId ->
            if (schoolClassId != null)
            {
                val scheduleBlock = ScheduleBlock(schoolClassId, Time(0, 0), Time(0, 0))
                MainActivity.schedule?.value?.get(dayOfWeek)?.add(scheduleBlock)
                val itemCount = binding.dayOfWeekCard.scheduleBlocks.adapter?.itemCount
                binding.dayOfWeekCard.scheduleBlocks.adapter?.notifyItemInserted(itemCount ?: 0)
            }
        }

        val dayOfWeekCard = DayOfWeekVH(binding.dayOfWeekCard)
        val schedule = MainActivity.schedule?.value?.get(dayOfWeek)

        val icon = AppCompatResources.getDrawable(applicationContext, R.drawable.delete_icon)
        if (icon != null) {
            val action = { adapterPosition: Int ->
                MainActivity.schedule?.value?.get(dayOfWeek)?.removeAt(adapterPosition)
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
                dayOfWeekCard.bind(schedule, dayOfWeek, schoolClassListSelector)
            }
        }

        val observer = Observer<Any> {
            val schedule = MainActivity.schedule?.value?.get(dayOfWeek)
            if (schedule != null)
                dayOfWeekCard.bind(schedule, dayOfWeek, schoolClassListSelector)
        }

        MainActivity.schoolClasses?.observe(this, observer)
        MainActivity.schedule?.observe(this, observer)
    }
}