package com.vtec.schooltime

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vtec.schooltime.activities.DayOfWeekEditActivity
import com.vtec.schooltime.databinding.DayOfWeekListItemBinding
import com.vtec.schooltime.fragments.adapterPosToDayOfWeek
import com.vtec.schooltime.fragments.dayOfWeekToAdapterPos
import java.util.*

class DayOfWeekListAdapter(private val schedule: SchoolSchedule, private val showWeekend: Boolean, private val adapterPosOfCurrentDayOfWeek: Int?) : RecyclerView.Adapter<DayOfWeekVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayOfWeekVH {
        val binding = DayOfWeekListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayOfWeekVH(binding)
    }

    override fun onBindViewHolder(holder: DayOfWeekVH, position: Int) {
        val dayOfWeek = adapterPosToDayOfWeek(position)
        if (adapterPosOfCurrentDayOfWeek == null) return
        schedule[dayOfWeek]?.let { holder.bind(it, dayOfWeek, adapterPosOfCurrentDayOfWeek, null) }
    }

    override fun getItemCount() = schedule.let { if (!showWeekend && it.size > 5) 5 else it.size }
}

class DayOfWeekVH(private val binding: DayOfWeekListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    private val context: Context = binding.root.context

    fun bind(schedule: DayOfWeekSchedule, dayOfWeek: Int, currentDayOfWeek: Int?, editLauncher: ActivityResultLauncher<Unit>?)
    {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        binding.dayOfWeek.text = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
        var currentScheduleBlockIndex : Int? = null

        if (editLauncher == null)
        {
            val icon = AppCompatResources.getDrawable(context, R.drawable.pen_icon)
            if (icon != null)
            {
                binding.edit.visibility = View.VISIBLE
                binding.edit.setOnClickListener {
                    val intent = Intent(context, DayOfWeekEditActivity::class.java).apply {
                        putExtra("day_of_week", adapterPosToDayOfWeek(adapterPosition))
                    }
                    context.startActivity(intent)
                }
            }
        }

        if (currentDayOfWeek != null && currentDayOfWeek == dayOfWeek)
        {
            binding.base.strokeWidth = 2
            binding.base.strokeColor = context.getColor(R.color.app_fg)
            binding.dayOfWeek.setTypeface(null, Typeface.BOLD)

            val scheduleBlockSearch = getCurrentScheduleBlock(MainActivity.schedule, MainActivity.subjects)
            if (scheduleBlockSearch.type == R.string.during_subject)
            {
                val scheduleBlocks = MainActivity.schedule.getOrDefault(currentDayOfWeek, mutableListOf())
                currentScheduleBlockIndex = scheduleBlocks.indexOf(scheduleBlockSearch.scheduleBlock)
            }
        }
        else
        {
            binding.base.strokeWidth = 0
            binding.dayOfWeek.setTypeface(null, Typeface.NORMAL)
        }

        val adapter = ScheduleBlockListAdapter(schedule, dayOfWeek, MainActivity.calculateSmallestScheduleBlockDelta(), currentScheduleBlockIndex)
        binding.scheduleBlocks.adapter = adapter
        binding.scheduleBlocks.layoutManager = LinearLayoutManager(context)

        if (adapter.itemCount != 0)
            binding.scheduleBlocks.visibility = View.VISIBLE
        else
            binding.scheduleBlocks.visibility = View.GONE

        if (editLauncher != null)
        {
            binding.addButton.visibility = View.VISIBLE
            binding.addButton.setOnClickListener {
                val vibrator = context.getSystemService(Vibrator::class.java)
                vibrator.vibrate(App.littleVibrationEffect)
                editLauncher.launch(Unit)
            }
        }
    }
}
