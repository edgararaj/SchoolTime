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
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtec.schooltime.*
import com.vtec.schooltime.activities.ClassEditActivity
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
        val adapter = ClassListAdapter(MainActivity.schoolClasses)

        val icon = AppCompatResources.getDrawable(requireContext(), R.drawable.delete_icon)
        if (icon != null)
        {
            val action = { adapterPosition: Int ->
                MainActivity.schoolClasses.value?.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
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

        MainActivity.schoolClasses.observe(viewLifecycleOwner, {
            adapter.notifyDataSetChanged()
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}