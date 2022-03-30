package com.vtec.schooltime.fragments

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtec.schooltime.*
import com.vtec.schooltime.activities.DayOfWeekEditActivity
import com.vtec.schooltime.databinding.FragmentScheduleBinding
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

fun adapterPosToDayOfWeek(pos: Int) = if (pos == 6) 1 else (pos + 2)
fun dayOfWeekToAdapterPos(dow: Int) = if (dow == 1) 6 else (dow - 2)

class ScheduleFragment : Fragment() {
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)

        val icon = AppCompatResources.getDrawable(requireContext(), R.drawable.edit_icon)
        if (icon != null)
        {
            val action = { adapterPosition: Int ->
                val intent = Intent(context, DayOfWeekEditActivity::class.java).apply {
                    putExtra("day_of_week", adapterPosToDayOfWeek(adapterPosition))
                }
                startActivity(intent)
            }
            val itemTouchHelper = ItemTouchHelper(ItemSlideAction(requireContext(), icon, false, action, null))
            itemTouchHelper.attachToRecyclerView(binding.daysOfWeek)
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val calendar = Calendar.getInstance()
        val adapterPosOfCurrentDayOfWeek = dayOfWeekToAdapterPos(calendar.get(Calendar.DAY_OF_WEEK))
        val adapter = DayOfWeekListAdapter(MainActivity.schedule, preferences.getBoolean("weekend_school", false), adapterPosOfCurrentDayOfWeek)

        binding.daysOfWeek.adapter = adapter
        binding.daysOfWeek.layoutManager = LinearLayoutManager(requireContext())
        binding.daysOfWeek.edgeEffectFactory = BounceEdgeEffectFactory()
        binding.daysOfWeek.addItemDecoration(ItemDecoration(1))

        binding.daysOfWeek.scrollToPosition(adapterPosOfCurrentDayOfWeek)

        binding.fabRestore.setOnClickListener {
            val file = File(context?.getExternalFilesDir(null), "classes.json")
            val outStream = FileOutputStream(file)
            Json.encodeToStream(fallbackSchoolClasses, outStream)
            MainActivity.schoolClasses = Json.decodeFromStream(FileInputStream(file))

            MainActivity.lessons.clear()
            run {
                val lessonsFile = File(context?.getExternalFilesDir(null), "lessons.json")
                val lessonsOutputStream = FileOutputStream(lessonsFile)
                Json.encodeToStream(fallbackSchoolLessons, lessonsOutputStream)
                val init = Json.decodeFromStream<SchoolLessons>(FileInputStream(lessonsFile))
                init.forEach { (t, u) -> MainActivity.lessons[t] = u }
            }

            MainActivity.schedule.clear()
            run {
                val scheduleFile = File(context?.getExternalFilesDir(null), "schedule.json")
                val scheduleOutputStream = FileOutputStream(scheduleFile)
                Json.encodeToStream(fallbackSchedule, scheduleOutputStream)
                val init = Json.decodeFromStream<SchoolSchedule>(FileInputStream(scheduleFile))
                init.forEach { (t, u) ->
                    if (MainActivity.schedule[t] == null) MainActivity.schedule[t] = mutableListOf()
                    u.forEach { MainActivity.schedule[t]?.add(it) }
                }
            }

            MainActivity.schedule.forEach { entry ->
                entry.value.sortBy { it.startTime }
            }

            MainActivity.didSchoolClassesUpdate.notify()
            MainActivity.didLessonsUpdate.notify()
            MainActivity.didSchedulesUpdate.notify()
        }

        binding.fabApply.setOnClickListener {
            val file = File(context?.getExternalFilesDir(null), "classes.json")
            val outStream = FileOutputStream(file)
            Json.encodeToStream(MainActivity.schoolClasses, outStream)

            val lessonsFile = File(context?.getExternalFilesDir(null), "lessons.json")
            val lessonsOutputStream = FileOutputStream(lessonsFile)
            Json.encodeToStream(MainActivity.lessons, lessonsOutputStream)

            val scheduleFile = File(context?.getExternalFilesDir(null), "schedule.json")
            val scheduleOutputStream = FileOutputStream(scheduleFile)
            Json.encodeToStream(MainActivity.schedule, scheduleOutputStream)
        }

        MainActivity.didLessonsUpdate.observe(viewLifecycleOwner, {
            adapter.notifyDataSetChanged()
        })

        MainActivity.didSchedulesUpdate.observe(viewLifecycleOwner, {
            adapter.notifyDataSetChanged()
        })

        MainActivity.didSchoolClassesUpdate.observe(viewLifecycleOwner, {
            adapter.notifyDataSetChanged()
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}