package com.vtec.schooltime.ui.schedule

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtec.schooltime.*
import com.vtec.schooltime.databinding.FragmentScheduleBinding

class ScheduleFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentScheduleBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentScheduleBinding.inflate(inflater, container, false)

        val icon = AppCompatResources.getDrawable(requireContext(), R.drawable.edit_icon)
        if (icon != null)
        {
            val action = { adapterPosition: Int ->
                val intent = Intent(context, DayOfWeekEditActivity::class.java).apply {
                    putExtra("day_of_week", MainActivity.schedule?.value?.keys?.toList()?.get(adapterPosition))
                }
                startActivity(intent)
            }
            val itemTouchHelper = ItemTouchHelper(ItemSlideAction(requireContext(), icon, action, null))
            itemTouchHelper.attachToRecyclerView(binding.daysOfWeek)
        }

        val adapter = MainActivity.schedule?.let { DayOfWeekListAdapter(it) }

        binding.daysOfWeek.adapter = adapter
        binding.daysOfWeek.layoutManager = LinearLayoutManager(requireContext())
        binding.daysOfWeek.edgeEffectFactory = BounceEdgeEffectFactory()
        binding.daysOfWeek.addItemDecoration(ItemDecoration())

        MainActivity.schoolClasses?.observe(viewLifecycleOwner, {
            adapter?.notifyDataSetChanged()
        })

        MainActivity.schedule?.observe(viewLifecycleOwner, {
            adapter?.notifyDataSetChanged()
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}