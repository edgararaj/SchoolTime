package com.vtec.schooltime

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vtec.schooltime.databinding.ClassListItemBinding

class ClassListAdapter(private val schoolClasses: SchoolClasses, private val activity: Activity?, private val mode: ClassVH.Mode) : RecyclerView.Adapter<ClassVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassVH {
        val binding = ClassListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClassVH(binding)
    }

    override fun onBindViewHolder(holder: ClassVH, position: Int) {
        schoolClasses.value?.toList()?.get(position)?.second?.let {
            holder.bind(it, activity, mode)
        }
    }

    override fun getItemCount() = schoolClasses.value?.size ?: 0
}

class ClassVH(private val binding: ClassListItemBinding) : RecyclerView.ViewHolder(binding.root) {
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

    enum class Mode {
        Display, EditOnClick, SelectAndFinishActivity
    }

    fun bind(schoolClass: SchoolClass?, activity: Activity?, mode: Mode)
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
                if (mode == Mode.EditOnClick)
                {
                    val intent = Intent(context, ClassEditActivity::class.java).apply {
                        putExtra("school_class_id", schoolClass.shortName)
                    }

                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(App.littleVibrationEffect)
                    context.startActivity(intent)
                }
                else
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
