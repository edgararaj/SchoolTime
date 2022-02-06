package com.vtec.schooltime.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.vtec.schooltime.BounceEdgeEffectFactory
import com.vtec.schooltime.CityListAdapter
import com.vtec.schooltime.ItemDecoration
import com.vtec.schooltime.MainActivity
import com.vtec.schooltime.databinding.FragmentWeatherBinding

class WeatherFragment : Fragment() {
    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)

        val adapter = CityListAdapter()
        binding.cities.adapter = adapter
        binding.cities.layoutManager = LinearLayoutManager(context)
        binding.cities.edgeEffectFactory = BounceEdgeEffectFactory()
        binding.cities.addItemDecoration(ItemDecoration(1))

        binding.cityNameEdit.doOnTextChanged { text, start, before, count ->
            adapter.filter.filter(text)
        }

        binding.cityNameEdit.setText(MainActivity.weatherLocation.value?.name)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}