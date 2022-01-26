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

class DayOfWeekListAdapter(private val schedule: SchoolSchedule, private val showWeekend: Boolean) : RecyclerView.Adapter<DayOfWeekVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayOfWeekVH {
        val binding = DayOfWeekListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayOfWeekVH(binding)
    }

    override fun onBindViewHolder(holder: DayOfWeekVH, position: Int) {
        val dayOfWeek = if (position == 6) 1 else (position + 2)
        schedule[dayOfWeek]?.let { holder.bind(it, dayOfWeek, null) }
    }

    override fun getItemCount() = schedule.let { if (!showWeekend && it.size > 5) 5 else it.size }
}

class DayOfWeekVH(private val binding: DayOfWeekListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    private val context: Context = binding.root.context

    fun bind(schedule: DayOfWeekSchedule, dayOfWeek: Int, editLauncher: ActivityResultLauncher<Unit>?)
    {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        binding.dayOfWeek.text = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())

        val adapter = ScheduleBlockListAdapter(schedule, dayOfWeek, MainActivity.calculateSmallestScheduleBlockDelta())
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
