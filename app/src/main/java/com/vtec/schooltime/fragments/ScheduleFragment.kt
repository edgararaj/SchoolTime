package com.vtec.schooltime.fragments

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtec.schooltime.*
import com.vtec.schooltime.activities.DayOfWeekEditActivity
import com.vtec.schooltime.activities.LessonListActivity
import com.vtec.schooltime.databinding.FragmentScheduleBinding
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.*
import java.util.*

fun adapterPosToDayOfWeek(pos: Int) = if (pos == 6) 1 else (pos + 2)
fun dayOfWeekToAdapterPos(dow: Int) = if (dow == 1) 6 else (dow - 2)

class ScheduleFragment : Fragment() {
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 12 && data?.data != null)
        {
            data.data?.let { uri ->
                val contentResolver = binding.root.context.contentResolver
                val data = readTextFromUri(uri, contentResolver).split("|")
                MainActivity.schedule.clear()
                run {
                    val init = Json.decodeFromString<SchoolSchedule>(data[0])
                    init.forEach { (t, u) ->
                        if (MainActivity.schedule[t] == null) MainActivity.schedule[t] = mutableListOf()
                        u.forEach { MainActivity.schedule[t]?.add(it) }
                    }
                }
                MainActivity.lessons.clear()
                run {
                    val init = Json.decodeFromString<SchoolLessons>(data[1])
                    init.forEach { (t, u) -> MainActivity.lessons[t] = u }
                }
            }
        }

        MainActivity.schedule.forEach { entry ->
            entry.value.sortBy { it.startTime }
        }

        MainActivity.didSchedulesUpdate.notify()
        MainActivity.didLessonsUpdate.notify()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)

        val icon = AppCompatResources.getDrawable(requireContext(), R.drawable.pen_icon)
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

        binding.fabLoadJson.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }

            startActivityForResult(intent, 12)
        }

        binding.fabApply.setOnClickListener {
            val lessonsFile = File(context?.getExternalFilesDir(null), "data.hdvt")
            lessonsFile.writeText(Json.encodeToString(MainActivity.schedule) + "|" + Json.encodeToString(MainActivity.lessons))
        }

        MainActivity.didLessonsUpdate.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
        }

        MainActivity.didSchedulesUpdate.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}