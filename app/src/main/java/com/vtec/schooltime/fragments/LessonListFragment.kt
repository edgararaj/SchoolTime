package com.vtec.schooltime.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
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
    private lateinit var classListSelectorDeletion: ActivityResultLauncher<Unit>
    private var schoolLessonId: String? = null

    private var onLessonDeleted: ((String) -> Unit)? = null
    private var onClassSelected = { schoolLessonId: String ->
        this.schoolLessonId = schoolLessonId
        classListSelector.launch(Unit)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        classListSelectorDeletion = registerForActivityResult(ClassListActivity.Contract()) { schoolClassId ->
            if (schoolClassId != null && onLessonDeleted != null) {
                onLessonDeleted?.let { it(schoolClassId) }
            }
        }

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

        val adapter = LessonListAdapter(MainActivity.lessons,
            onClassSelected, if (activity is LessonListActivity) LessonVH.Mode.SelectAndFinishActivity else LessonVH.Mode.EditOnClick)

        val icon = AppCompatResources.getDrawable(requireContext(), R.drawable.delete_icon)
        if (icon != null)
        {
            val action = { adapterPosition: Int ->
                val schoolLesson = MainActivity.lessons.toList()[adapterPosition]

                onLessonDeleted = { schoolClassId ->
                    MainActivity.schedule.forEach { (dayOfWeek, dayOfWeekSchedule) ->
                        val newDayOfWeekSchedule: DayOfWeekSchedule = mutableListOf()
                        dayOfWeekSchedule.forEach { scheduleBlock ->
                            if (scheduleBlock.schoolLessonId != schoolLesson.first || scheduleBlock.schoolClassId != schoolClassId)
                            {
                                newDayOfWeekSchedule.add(scheduleBlock)
                            }
                        }
                        MainActivity.schedule[dayOfWeek] = newDayOfWeekSchedule
                    }

                    MainActivity.didSchedulesUpdate.notify()
                    //MainActivity.lessons.remove(schoolLesson.first)
                    //adapter.notifyItemRemoved(adapterPosition)
                }

                classListSelectorDeletion.launch(Unit)
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