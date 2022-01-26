package com.vtec.schooltime

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.vtec.schooltime.databinding.CityCardBinding
import com.vtec.schooltime.databinding.UniversalCardBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URL
import java.util.logging.LogRecord

class CityListAdapter : RecyclerView.Adapter<CityVH>() {
    val cityNamesList: MutableList<LocationEntry> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityVH {
        val binding = CityCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CityVH(binding)
    }

    override fun onBindViewHolder(holder: CityVH, position: Int) {
        holder.bind(cityNamesList[position], { notifyDataSetChanged() })
    }

    override fun getItemCount() = cityNamesList.size

    var filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val apikey = "cc752389952464c89015c0c7aa74fab1"

            cityNamesList.clear()
            try {
                val count = 5
                val location = URL("https://api.openweathermap.org/geo/1.0/direct?q=$constraint&limit=$count&appid=$apikey").readText()
                for (i in 0 until count)
                {
                    val entry = JSONArray(location).getJSONObject(i)
                    val name = entry.getString("name")
                    val country = entry.getString("country")
                    val lat = entry.getDouble("lat")
                    val lon = entry.getDouble("lon")
                    cityNamesList.add(LocationEntry(name, country, lat, lon))
                }
            } catch (ex: FileNotFoundException) {
                Log.d("HTTPS", "Failed to fetch city names!")
            }

            return FilterResults()
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            notifyDataSetChanged()
        }
    }
}

class CityVH(private val binding: CityCardBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(locationEntry: LocationEntry, onClick: () -> Unit)
    {
        binding.cityName.text = "${locationEntry.name} ${locationEntry.country}"
        if (locationEntry == MainActivity.weatherLocation.value)
            binding.check.visibility = View.VISIBLE
        else
        {
            binding.check.visibility = View.GONE
            binding.root.setOnClickListener {
                MainActivity.weatherLocation.value = locationEntry
                binding.check.visibility = View.VISIBLE
                onClick()
            }
        }
    }
}