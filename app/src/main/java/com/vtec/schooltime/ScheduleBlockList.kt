package com.vtec.schooltime

import android.content.Context
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.os.Build
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.vtec.schooltime.databinding.ScheduleBlockListItemBinding

class ScheduleBlockListAdapter(private val dayOfWeekSchedule: DayOfWeekSchedule, private val dayOfWeek: Int, private val onStartDrag: ((RecyclerView.ViewHolder) -> Unit)?) : RecyclerView.Adapter<ScheduleBlockVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleBlockVH {
        val binding = ScheduleBlockListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleBlockVH(binding)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: ScheduleBlockVH, position: Int) {
        holder.bind(dayOfWeekSchedule.getOrNull(position), dayOfWeek, onStartDrag)
    }

    override fun getItemCount() = dayOfWeekSchedule.size
}

class ScheduleBlockVH(private val binding: ScheduleBlockListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    private val context: Context = binding.root.context

    @RequiresApi(Build.VERSION_CODES.Q)
    fun bind(scheduleBlock: ScheduleBlock?, dayOfWeek: Int, onStartDrag: ((RecyclerView.ViewHolder) -> Unit)?)
    {
        if (scheduleBlock != null)
        {
            val schoolClass = MainActivity.schoolClasses?.value?.get(scheduleBlock.schoolClassId)
            if (schoolClass != null)
            {
                val bgColor = schoolClass.color
                val contrastyFgColor = getContrastingColor(bgColor)
                binding.root.setCardBackgroundColor(bgColor)
                binding.className.setTextColor(contrastyFgColor)
                binding.className.text = schoolClass.longName

                binding.innerCard.setCardBackgroundColor(getDarkerColor(bgColor))
                binding.blockTime.setTextColor(contrastyFgColor)
                binding.blockTime.text = "${scheduleBlock.startTime} — ${scheduleBlock.endTime}"

                if (onStartDrag != null)
                {
                    binding.dragHandle.visibility = View.VISIBLE
                    binding.dragHandle.colorFilter = BlendModeColorFilter(contrastyFgColor, BlendMode.SRC_ATOP)
                    binding.dragHandle.setOnTouchListener { view, motionEvent ->
                        if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN)
                            onStartDrag(this)
                        false
                    }
                }

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
