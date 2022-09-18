package com.vtec.schooltime.fragments

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtec.schooltime.*
import com.vtec.schooltime.activities.DayOfWeekEditActivity
import com.vtec.schooltime.activities.SubjectListActivity
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val adapter = DayOfWeekListAdapter(MainActivity.schedule, preferences.getBoolean("weekend_school", false))

        binding.daysOfWeek.adapter = adapter
        binding.daysOfWeek.layoutManager = LinearLayoutManager(requireContext())
        binding.daysOfWeek.edgeEffectFactory = BounceEdgeEffectFactory()
        binding.daysOfWeek.addItemDecoration(ItemDecoration(1))

        MainActivity.didSubjectsUpdate.observe(viewLifecycleOwner) {
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