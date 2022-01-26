package com.vtec.schooltime.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.vtec.schooltime.*
import com.vtec.schooltime.activities.ClassEditActivity
import com.vtec.schooltime.activities.ClassListActivity
import com.vtec.schooltime.databinding.FragmentClassListBinding

class ClassListFragment : Fragment() {
    private var _binding: FragmentClassListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClassListBinding.inflate(inflater, container, false)

        MainActivity.schoolClasses.let {
            val adapter = ClassListAdapter(it, activity, if (activity is ClassListActivity) ClassVH.Mode.SelectAndFinishActivity else ClassVH.Mode.EditOnClick)

            val icon = AppCompatResources.getDrawable(requireContext(), R.drawable.delete_icon)
            if (icon != null)
            {
                val action = { adapterPosition: Int ->
                    val schoolClassId = it.toList()[adapterPosition].first
                    MainActivity.schedule.forEach { (dayOfWeek, dayOfWeekSchedule) ->
                        val newDayOfWeekSchedule: DayOfWeekSchedule = mutableListOf()
                        dayOfWeekSchedule.forEach { scheduleBlock ->
                            if (scheduleBlock.schoolClassId != schoolClassId)
                            {
                                newDayOfWeekSchedule.add(scheduleBlock)
                            }
                        }
                        MainActivity.schedule[dayOfWeek] = newDayOfWeekSchedule
                    }
                    MainActivity.didSchedulesUpdate.notify()

                    MainActivity.schoolClasses.remove(schoolClassId)
                    adapter.notifyItemRemoved(adapterPosition)
                }
                val itemTouchHelper = ItemTouchHelper(ItemSlideAction(requireContext(), icon, true, action, null))
                itemTouchHelper.attachToRecyclerView(binding.schoolClasses)
            }

            binding.schoolClasses.adapter = adapter
            binding.schoolClasses.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
            binding.schoolClasses.edgeEffectFactory = BounceEdgeEffectFactory()
            binding.schoolClasses.addItemDecoration(ItemDecoration(2))

            binding.fab.root.setOnClickListener {
                val intent = Intent(requireContext(), ClassEditActivity::class.java).apply { }

                val vibrator = requireContext().getSystemService(Vibrator::class.java)
                vibrator.vibrate(App.littleVibrationEffect)
                startActivity(intent)
            }
            binding.fab.root.show()

            MainActivity.didSchoolClassesUpdate.observe(viewLifecycleOwner, {
                adapter.notifyDataSetChanged()
            })
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}