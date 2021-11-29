package com.vtec.schooltime

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vtec.schooltime.databinding.SchoolClassListItemBinding

class SchoolClassListAdapter(private val schoolClasses: SchoolClasses, private val activity: Activity?) : RecyclerView.Adapter<SchoolClassVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchoolClassVH {
        val binding = SchoolClassListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SchoolClassVH(binding)
    }

    override fun onBindViewHolder(holder: SchoolClassVH, position: Int) {
        schoolClasses.value?.toList()?.get(position)?.second?.let {
            if (activity != null)
            {
                holder.bind(it, SchoolClassVH.Mode.SelectAndFinishActivity)
                holder.activity = activity
            }
            else
                holder.bind(it, SchoolClassVH.Mode.EditOnClick)
        }
    }

    override fun getItemCount() = schoolClasses.value?.size ?: 0
}

class SchoolClassVH(private val binding: SchoolClassListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    private val context: Context = binding.root.context

    var color: Int = Color.BLACK
    set(value) {
        binding.root.setCardBackgroundColor(value)
        val contrastyFgColor = getContrastingColor(value)
        binding.className.setTextColor(contrastyFgColor)
        field = value
    }

    var longName: String = context.getString(R.string.unnamed)
    set(value) {
        binding.className.text = value
        field = value
    }

    var activity: Activity? = null

    enum class Mode {
        Display, EditOnClick, SelectAndFinishActivity
    }

    fun bind(schoolClass: SchoolClass?, mode: Mode)
    {
        if (schoolClass == null)
        {
            longName = longName
            color = color
        }
        else
        {
            longName = schoolClass.longName
            color = schoolClass.color

            if (mode != Mode.Display) binding.root.setOnClickListener {
//                if (mode == Mode.EditOnClick)
//                {
//                    val intent = Intent(context, SchoolClassEditActivity::class.java).apply {
//                        putExtra("school_class_id", schoolClass.shortName)
//                    }
//
//                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//                    vibrator.vibrate(App.littleVibrationEffect)
//                    context.startActivity(intent)
//                }
//                else
                {
                    activity?.let {
                        it.setResult(0, Intent().putExtra("school_class_id", schoolClass.shortName))
                        it.finish()
                    }
                }
            }
        }
    }
}
