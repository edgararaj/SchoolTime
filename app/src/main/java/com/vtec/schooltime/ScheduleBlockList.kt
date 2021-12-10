package com.vtec.schooltime

import android.content.Context
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vtec.schooltime.databinding.ScheduleBlockListItemBinding
import kotlin.math.round

class ScheduleBlockListAdapter(private val dayOfWeekSchedule: DayOfWeekSchedule, private val dayOfWeek: Int) : RecyclerView.Adapter<ScheduleBlockVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleBlockVH {
        val binding = ScheduleBlockListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleBlockVH(binding)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: ScheduleBlockVH, position: Int) {
        holder.bind(dayOfWeekSchedule.getOrNull(position), dayOfWeek)
    }

    override fun getItemCount() = dayOfWeekSchedule.size
}

class ScheduleBlockVH(val binding: ScheduleBlockListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    private val context: Context = binding.root.context

    @RequiresApi(Build.VERSION_CODES.Q)
    fun bind(scheduleBlock: ScheduleBlock?, dayOfWeek: Int)
    {
        if (scheduleBlock != null)
        {
            val schoolClass = MainActivity.schoolClasses?.value?.get(scheduleBlock.schoolClassId)
            if (schoolClass != null)
            {
                val bgColor = schoolClass.color
                val contrastyFgColor = getContrastingColor(bgColor)
                binding.root.setBackgroundColor(bgColor)
                binding.className.setTextColor(contrastyFgColor)
                binding.className.text = schoolClass.longName

                val darkerBgColor = getDarkerColor(bgColor)
                binding.root.strokeColor = getDarkerColor(getDarkerColor(darkerBgColor))
                binding.innerCard.setBackgroundColor(darkerBgColor)

                binding.startTime.setTextColor(contrastyFgColor)
                binding.startTime.text = scheduleBlock.startTime.toString()

                binding.endTime.setTextColor(contrastyFgColor)
                binding.endTime.text = scheduleBlock.endTime.toString()

                binding.root.setOnClickListener {
                    val intent = Intent(context, ClassEditActivity::class.java).apply {
                        putExtra("school_class_id", scheduleBlock.schoolClassId)
                    }

                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(App.littleVibrationEffect)
                    context.startActivity(intent)
                }

                binding.innerCard.setOnClickListener {
                    val intent = Intent(context, ScheduleBlockEditActivity::class.java).apply {
                        putExtra("day_of_week", dayOfWeek)
                        putExtra("schedule_block_position", adapterPosition)
                    }

                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(App.littleVibrationEffect)
                    context.startActivity(intent)
                }
            }
        }
    }
}
