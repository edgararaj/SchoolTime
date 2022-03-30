package com.vtec.schooltime

import android.content.Context
import android.content.Intent
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vtec.schooltime.activities.LessonEditActivity
import com.vtec.schooltime.activities.ScheduleBlockEditActivity
import com.vtec.schooltime.databinding.ScheduleBlockListItemBinding

class ScheduleBlockListAdapter(private val dayOfWeekSchedule: DayOfWeekSchedule, private val dayOfWeek: Int, private val smallestScheduleBlockDelta: Int) : RecyclerView.Adapter<ScheduleBlockVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleBlockVH {
        val binding = ScheduleBlockListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleBlockVH(binding)
    }

    override fun onBindViewHolder(holder: ScheduleBlockVH, position: Int) {
        val scheduleBlock = dayOfWeekSchedule.getOrNull(position)
        if (scheduleBlock != null)
        {
            val stretch = (scheduleBlock.delta.averageHour - smallestScheduleBlockDelta) * 30
            holder.bind(scheduleBlock, dayOfWeek, stretch)
        }
    }

    override fun getItemCount() = dayOfWeekSchedule.size
}

class ScheduleBlockVH(val binding: ScheduleBlockListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    private val context: Context = binding.root.context

    fun bind(scheduleBlock: ScheduleBlock, dayOfWeek: Int, stretch: Int)
    {
        val schoolLesson = MainActivity.lessons[scheduleBlock.schoolLessonId]
        if (schoolLesson != null)
        {
            val bgColor = schoolLesson.color
            val contrastyFgColor = getContrastingColor(bgColor)
            binding.root.setBackgroundColor(bgColor)
            binding.lessonName.setTextColor(contrastyFgColor)
            binding.lessonName.text = schoolLesson.longName
            if (MainActivity.schoolClasses.size != 1)
            {
                binding.className.setTextColor(contrastyFgColor)
                binding.className.text = scheduleBlock.schoolClassId
            }

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
                val intent = Intent(context, LessonEditActivity::class.java).apply {
                    putExtra("school_lesson_id", scheduleBlock.schoolLessonId)
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
