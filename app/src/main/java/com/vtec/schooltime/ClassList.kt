package com.vtec.schooltime

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vtec.schooltime.activities.ClassEditActivity
import com.vtec.schooltime.databinding.DisplayCardBinding

class ClassListAdapter(private val schoolClasses: SchoolClasses) : RecyclerView.Adapter<ClassVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassVH {
        val binding = DisplayCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClassVH(binding)
    }

    override fun onBindViewHolder(holder: ClassVH, position: Int) {
        holder.bind(schoolClasses, position, ClassVH.Mode.EditOnClick)
    }

    override fun getItemCount() = schoolClasses.value?.size ?: 0
}

class ClassVH(private val binding: DisplayCardBinding) : RecyclerView.ViewHolder(binding.root) {
    private val context: Context = binding.root.context

    var color: Int = Color.BLACK
        set(value) {
            binding.root.setCardBackgroundColor(value)
            val contrastyFgColor = getContrastingColor(value)
            binding.name.setTextColor(contrastyFgColor)
            field = value
        }

    var name: String = context.getString(R.string.unnamed)
        set(value) {
            binding.name.text = value
            field = value
        }

    enum class Mode {
        Display, EditOnClick
    }

    fun bind(schoolClasses: SchoolClasses, position: Int, mode: Mode)
    {
        if (position == -1)
        {
            name = name
            color = color
        }
        else
        {
            val schoolClass = schoolClasses.value?.get(position)
            if (schoolClass != null) {
                name = schoolClass.name
                color = schoolClass.color

                if (mode != Mode.Display) binding.root.setOnClickListener {
                    val intent = Intent(context, ClassEditActivity::class.java).apply {
                        putExtra("school_class_id", position)
                    }

                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(App.littleVibrationEffect)
                    context.startActivity(intent)
                }
            }
        }
    }
}
