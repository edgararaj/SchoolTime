package com.vtec.schooltime.fragments

import android.content.Intent
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
                    putExtra("day_of_week", adapterPosition + 2)
                }
                startActivity(intent)
            }
            val itemTouchHelper = ItemTouchHelper(ItemSlideAction(requireContext(), icon, false, action, null))
            itemTouchHelper.attachToRecyclerView(binding.daysOfWeek)
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val adapter = DayOfWeekListAdapter(MainActivity.schedule, preferences.getBoolean("weekend_school", false))

        binding.daysOfWeek.adapter = adapter
        binding.daysOfWeek.layoutManager = LinearLayoutManager(requireContext())
        binding.daysOfWeek.edgeEffectFactory = BounceEdgeEffectFactory()
        binding.daysOfWeek.addItemDecoration(ItemDecoration(1))

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