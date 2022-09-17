package com.vtec.schooltime.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtec.schooltime.*
import com.vtec.schooltime.activities.LessonEditActivity
import com.vtec.schooltime.activities.LessonListActivity
import com.vtec.schooltime.databinding.FragmentLessonListBinding
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class LessonListFragment : Fragment() {
    private var _binding: FragmentLessonListBinding? = null
    private val binding get() = _binding!!

    private var onLessonSelected = { schoolLessonId: String ->
        activity?.setResult(0, Intent().putExtra("school_lesson_id", schoolLessonId))
        activity?.finish()
        Unit
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonListBinding.inflate(inflater, container, false)

        val adapter = LessonListAdapter(MainActivity.lessons, onLessonSelected, if (activity is LessonListActivity) LessonVH.Mode.SelectAndFinishActivity else LessonVH.Mode.EditOnClick)

        val icon = AppCompatResources.getDrawable(requireContext(), R.drawable.del_sweep_icon)
        if (icon != null)
        {
            val action = { adapterPosition: Int ->
                val schoolLesson = MainActivity.lessons.toList()[adapterPosition]

                MainActivity.schedule.forEach { (dayOfWeek, dayOfWeekSchedule) ->
                    val newDayOfWeekSchedule: DayOfWeekSchedule = mutableListOf()
                    dayOfWeekSchedule.forEach { scheduleBlock ->
                        if (scheduleBlock.id != schoolLesson.first)
                        {
                            newDayOfWeekSchedule.add(scheduleBlock)
                        }
                    }
                    MainActivity.schedule[dayOfWeek] = newDayOfWeekSchedule
                }

                MainActivity.didSchedulesUpdate.notify()
                MainActivity.lessons.remove(schoolLesson.first)
                adapter.notifyItemRemoved(adapterPosition)
            }
            val itemTouchHelper = ItemTouchHelper(ItemSlideAction(requireContext(), icon, true, action, null))
            itemTouchHelper.attachToRecyclerView(binding.schoolLessons)
        }

        binding.schoolLessons.adapter = adapter
        binding.schoolLessons.layoutManager = LinearLayoutManager(requireContext())
        binding.schoolLessons.edgeEffectFactory = BounceEdgeEffectFactory()
        binding.schoolLessons.addItemDecoration(ItemDecoration(1))

        binding.fab.root.setOnClickListener {
            val intent = Intent(requireContext(), LessonEditActivity::class.java).apply { }

            val vibrator = requireContext().getSystemService(Vibrator::class.java)
            vibrator.vibrate(App.littleVibrationEffect)
            startActivity(intent)
        }
        binding.fab.root.show()

        MainActivity.didLessonsUpdate.observe(viewLifecycleOwner, {
            adapter.notifyDataSetChanged()
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}