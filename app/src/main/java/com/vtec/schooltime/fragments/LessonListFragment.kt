package com.vtec.schooltime.fragments

import android.content.Context
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
import com.vtec.schooltime.activities.ClassListActivity
import com.vtec.schooltime.activities.LessonEditActivity
import com.vtec.schooltime.activities.LessonListActivity
import com.vtec.schooltime.databinding.FragmentLessonListBinding

class LessonListFragment : Fragment() {
    private var _binding: FragmentLessonListBinding? = null
    private val binding get() = _binding!!

    private lateinit var classListSelector: ActivityResultLauncher<Unit>
    private var schoolLessonId: String? = null
    private var onClassSelected = { schoolLessonId: String ->
        this.schoolLessonId = schoolLessonId
        classListSelector.launch(Unit)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        classListSelector = registerForActivityResult(ClassListActivity.Contract()) { schoolClassId ->
            activity?.setResult(0, Intent().putExtra("school_lesson_id", schoolLessonId).putExtra("school_class_id", schoolClassId))
            activity?.finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonListBinding.inflate(inflater, container, false)

        val adapter = LessonListAdapter(MainActivity.lessons, onClassSelected, if (activity is LessonListActivity) LessonVH.Mode.SelectAndFinishActivity else LessonVH.Mode.EditOnClick)

        val icon = AppCompatResources.getDrawable(requireContext(), R.drawable.delete_icon)
        if (icon != null)
        {
            val action = { adapterPosition: Int ->
                val schoolLesson = MainActivity.lessons.toList()[adapterPosition]

                MainActivity.schedule.forEach { (dayOfWeek, dayOfWeekSchedule) ->
                    val newDayOfWeekSchedule: DayOfWeekSchedule = mutableListOf()
                    dayOfWeekSchedule.forEach { scheduleBlock ->
                        if (scheduleBlock.schoolLessonId != schoolLesson.first)
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

        binding.fab.setOnClickListener {
            val intent = Intent(requireContext(), LessonEditActivity::class.java).apply { }

            val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(App.littleVibrationEffect)
            startActivity(intent)
        }
        binding.fab.show()

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