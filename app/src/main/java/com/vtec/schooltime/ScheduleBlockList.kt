package com.vtec.schooltime

import android.content.Context
import android.content.Intent
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.marginLeft
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.vtec.schooltime.activities.SubjectEditActivity
import com.vtec.schooltime.activities.ScheduleBlockEditActivity
import com.vtec.schooltime.databinding.ScheduleBlockListItemBinding

class ScheduleBlockListAdapter(private val dayOfWeekSchedule: DayOfWeekSchedule, private val dayOfWeek: Int, private val smallestScheduleBlockDelta: Int, private val currentScheduleBlockIndex: Int?) : RecyclerView.Adapter<ScheduleBlockVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleBlockVH {
        val binding = ScheduleBlockListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleBlockVH(binding)
    }

    override fun onBindViewHolder(holder: ScheduleBlockVH, position: Int) {
        val scheduleBlock = dayOfWeekSchedule.getOrNull(position)
        if (scheduleBlock != null)
        {
            val stretch = (scheduleBlock.duration.averageHour - smallestScheduleBlockDelta) * 30
            holder.bind(scheduleBlock, dayOfWeek, stretch, currentScheduleBlockIndex == position)
        }
    }

    override fun getItemCount() = dayOfWeekSchedule.size
}

class ScheduleBlockVH(val binding: ScheduleBlockListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    private val context: Context = binding.root.context

    fun bind(scheduleBlock: ScheduleBlock, dayOfWeek: Int, stretch: Int, currentBlock: Boolean)
    {
        val schoolSubject = MainActivity.subjects[scheduleBlock.id]
        if (schoolSubject != null)
        {
            val bgColor = schoolSubject.color
            val contrastyFgColor = getContrastingColor(bgColor)
            binding.root.setBackgroundColor(bgColor)
            if (currentBlock)
            {
                (binding.root.layoutParams as ViewGroup.MarginLayoutParams).setMargins(10, -15, 10, 10)
            }
            else
            {
                (binding.root.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 0, 0, 2)
            }
            binding.subjectName.setTextColor(contrastyFgColor)
            binding.subjectName.text = schoolSubject.longName

            val darkerBgColor = getDarkerColor(bgColor)
            //binding.root.strokeColor = getDarkerColor(getDarkerColor(darkerBgColor))
            binding.innerCard.setBackgroundColor(darkerBgColor)

            binding.startTime.setTextColor(contrastyFgColor)
            binding.startTime.text = scheduleBlock.startTime.toString()
            binding.startTime.setPadding(0, 0, 0, stretch)

            binding.endTime.setTextColor(contrastyFgColor)
            binding.endTime.text = scheduleBlock.endTime.toString()

            val vibrator = context.getSystemService(Vibrator::class.java)
            binding.root.setOnClickListener {
                val intent = Intent(context, SubjectEditActivity::class.java).apply {
                    putExtra("school_subject_id", scheduleBlock.id)
                }

                vibrator.vibrate(App.littleVibrationEffect)
                context.startActivity(intent)
            }

            binding.innerCard.setOnClickListener {
                val intent = Intent(context, ScheduleBlockEditActivity::class.java).apply {
                    putExtra("day_of_week", dayOfWeek)
                    putExtra("schedule_block_position", adapterPosition)
                }

                vibrator.vibrate(App.littleVibrationEffect)
                context.startActivity(intent)
            }
        }
    }
}
