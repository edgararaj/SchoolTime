package com.vtec.schooltime.ui.class_list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtec.schooltime.*
import com.vtec.schooltime.databinding.FragmentClassListBinding

class ClassListFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private var _binding: FragmentClassListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentClassListBinding.inflate(inflater, container, false)
        val adapter = MainActivity.schoolClasses?.let { ClassListAdapter(it, activity, if (activity is ClassListActivity) ClassVH.Mode.SelectAndFinishActivity else ClassVH.Mode.EditOnClick) }

        val icon = AppCompatResources.getDrawable(requireContext(), R.drawable.delete_icon)
        if (icon != null)
        {
            val action = { adapterPosition: Int ->
                val schoolClass = MainActivity.schoolClasses?.value?.toList()?.get(adapterPosition)
                if (schoolClass != null)
                {
                    MainActivity.schoolClasses?.value?.remove(schoolClass.first)

                    val newSchedule: MutableMap<Int, DayOfWeekSchedule> = mutableMapOf()
                    MainActivity.schedule?.value?.forEach { (dayOfWeek, dayOfWeekSchedule) ->
                        val newDayOfWeekSchedule: DayOfWeekSchedule = mutableListOf()
                        dayOfWeekSchedule.forEach { scheduleBlock ->
                            if (scheduleBlock.schoolClassId != schoolClass.first)
                                newDayOfWeekSchedule.add(scheduleBlock)
                        }
                        newSchedule[dayOfWeek] = newDayOfWeekSchedule
                    }
                    MainActivity.schedule?.value = newSchedule

                    adapter?.notifyItemRemoved(adapterPosition)
                }
            }
            val itemTouchHelper = ItemTouchHelper(ItemSlideAction(requireContext(), icon, true, action, null))
            itemTouchHelper.attachToRecyclerView(binding.schoolClasses)
        }

        binding.schoolClasses.adapter = adapter
        binding.schoolClasses.layoutManager = LinearLayoutManager(requireContext())
        binding.schoolClasses.edgeEffectFactory = BounceEdgeEffectFactory()
        binding.schoolClasses.addItemDecoration(ItemDecoration(R.dimen.screen_bottom_margin))

        binding.fab.setOnClickListener {
            val intent = Intent(requireContext(), ClassEditActivity::class.java).apply { }

            val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(App.littleVibrationEffect)
            startActivity(intent)
        }
        binding.fab.show()

        MainActivity.schoolClasses?.observe(viewLifecycleOwner, {
            adapter?.notifyDataSetChanged()
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}