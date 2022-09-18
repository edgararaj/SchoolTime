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
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtec.schooltime.*
import com.vtec.schooltime.activities.SubjectEditActivity
import com.vtec.schooltime.activities.SubjectListActivity
import com.vtec.schooltime.databinding.FragmentSubjectListBinding
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SubjectListFragment : Fragment() {
    private var _binding: FragmentSubjectListBinding? = null
    private val binding get() = _binding!!

    private var onSubjectSelected = { schoolSubjectId: String ->
        activity?.setResult(0, Intent().putExtra("school_subject_id", schoolSubjectId))
        activity?.finish()
        Unit
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectListBinding.inflate(inflater, container, false)

        val adapter = SubjectListAdapter(MainActivity.subjects, onSubjectSelected, if (activity is SubjectListActivity) SubjectVH.Mode.SelectAndFinishActivity else SubjectVH.Mode.EditOnClick)

        val icon = AppCompatResources.getDrawable(requireContext(), R.drawable.del_sweep_icon)
        if (icon != null)
        {
            val action = { adapterPosition: Int ->
                val schoolSubject = MainActivity.subjects.toList()[adapterPosition]

                MainActivity.schedule.forEach { (dayOfWeek, dayOfWeekSchedule) ->
                    val newDayOfWeekSchedule: DayOfWeekSchedule = mutableListOf()
                    dayOfWeekSchedule.forEach { scheduleBlock ->
                        if (scheduleBlock.id != schoolSubject.first)
                        {
                            newDayOfWeekSchedule.add(scheduleBlock)
                        }
                    }
                    MainActivity.schedule[dayOfWeek] = newDayOfWeekSchedule
                }

                MainActivity.didSchedulesUpdate.notify()
                MainActivity.subjects.remove(schoolSubject.first)
                adapter.notifyItemRemoved(adapterPosition)
            }
            val itemTouchHelper = ItemTouchHelper(ItemSlideAction(requireContext(), icon, true, action, null))
            itemTouchHelper.attachToRecyclerView(binding.schoolSubjects)
        }

        binding.schoolSubjects.adapter = adapter
        binding.schoolSubjects.layoutManager = LinearLayoutManager(requireContext())
        binding.schoolSubjects.edgeEffectFactory = BounceEdgeEffectFactory()
        binding.schoolSubjects.addItemDecoration(ItemDecoration(1))

        binding.fab.root.setOnClickListener {
            val intent = Intent(requireContext(), SubjectEditActivity::class.java).apply { }

            val vibrator = requireContext().getSystemService(Vibrator::class.java)
            vibrator.vibrate(App.littleVibrationEffect)
            startActivity(intent)
        }
        binding.fab.root.show()

        MainActivity.didSubjectsUpdate.observe(viewLifecycleOwner, {
            adapter.notifyDataSetChanged()
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}