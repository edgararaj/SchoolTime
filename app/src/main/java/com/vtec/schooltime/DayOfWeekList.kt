package com.vtec.schooltime

import android.content.Context
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vtec.schooltime.databinding.DayOfWeekListItemBinding
import java.util.*

class DayOfWeekListAdapter(private val schedule: Schedule) : RecyclerView.Adapter<DayOfWeekVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayOfWeekVH {
        val binding = DayOfWeekListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayOfWeekVH(binding)
    }

    override fun onBindViewHolder(holder: DayOfWeekVH, position: Int) {
        val entry = schedule.value?.toList()?.get(position)
        if (entry != null) {
            holder.bind(entry.second, entry.first, null, null)
        }
    }

    override fun getItemCount() = schedule.value?.size ?: 0
}

class DayOfWeekVH(private val binding: DayOfWeekListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    private val context: Context = binding.root.context

    fun bind(schedule: DayOfWeekSchedule, dayOfWeek: Int, editLauncher: ActivityResultLauncher<Unit>?, onStartDrag: ((RecyclerView.ViewHolder) -> Unit)?)
    {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        binding.dayOfWeek.text = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
        binding.scheduleBlocks.adapter = ScheduleBlockListAdapter(schedule, dayOfWeek, onStartDrag)
        binding.scheduleBlocks.layoutManager = LinearLayoutManager(context)
        if (editLauncher != null)
        {
            binding.addButton.visibility = View.VISIBLE
            binding.addButton.setOnClickListener {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(App.littleVibrationEffect)
                editLauncher.launch(Unit)
            }
        }
    }
}