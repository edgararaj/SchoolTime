package com.vtec.schooltime

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vtec.schooltime.activities.ClassEditActivity
import com.vtec.schooltime.databinding.UniversalCardBinding

class ClassListAdapter(private val schoolClasses: SchoolClasses, private val activity: Activity?, private val mode: ClassVH.Mode) : RecyclerView.Adapter<ClassVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassVH {
        val binding = UniversalCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClassVH(binding)
    }

    override fun onBindViewHolder(holder: ClassVH, position: Int) {
        holder.bind(schoolClasses.toList()[position], activity, mode)
    }

    override fun getItemCount() = schoolClasses.size
}

class ClassVH(private val binding: UniversalCardBinding) : RecyclerView.ViewHolder(binding.root) {
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
        Display, EditOnClick, SelectAndFinishActivity
    }

    fun bind(schoolClass: Pair<String?, SchoolClass?>, activity: Activity?, mode: Mode)
    {
        if (schoolClass.first == null)
        {
            name = name
            color = color
        }
        else
        {
            name = schoolClass.first!!
            color = schoolClass.second!!.color

            if (mode != Mode.Display) binding.root.setOnClickListener {
                if (mode == Mode.EditOnClick)
                {
                    val intent = Intent(context, ClassEditActivity::class.java).apply {
                        putExtra("school_class_id", schoolClass.first)
                    }

                    val vibrator = context.getSystemService(Vibrator::class.java)
                    vibrator.vibrate(App.littleVibrationEffect)
                    context.startActivity(intent)
                }
                else
                {
                    activity?.let {
                        it.setResult(0, Intent().putExtra("school_class_id", schoolClass.first))
                        it.finish()
                    }
                }
            }
        }
    }
}
