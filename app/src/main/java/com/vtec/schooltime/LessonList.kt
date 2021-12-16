package com.vtec.schooltime

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vtec.schooltime.activities.LessonEditActivity
import com.vtec.schooltime.databinding.DisplayCardBinding

class LessonListAdapter(private val schoolLessons: SchoolLessons, private val activity: Activity?, private val mode: LessonVH.Mode) : RecyclerView.Adapter<LessonVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonVH {
        val binding = DisplayCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LessonVH(binding)
    }

    override fun onBindViewHolder(holder: LessonVH, position: Int) {
        schoolLessons.value?.toList()?.get(position)?.second?.let {
            holder.bind(it, activity, mode)
        }
    }

    override fun getItemCount() = schoolLessons.value?.size ?: 0
}

class LessonVH(private val binding: DisplayCardBinding) : RecyclerView.ViewHolder(binding.root) {
    private val context: Context = binding.root.context

    var color: Int = Color.BLACK
        set(value) {
            binding.root.setCardBackgroundColor(value)
            val contrastyFgColor = getContrastingColor(value)
            binding.name.setTextColor(contrastyFgColor)
            field = value
        }

    var longName: String = context.getString(R.string.unnamed)
        set(value) {
            binding.name.text = value
            field = value
        }

    enum class Mode {
        Display, EditOnClick, SelectAndFinishActivity
    }

    fun bind(schoolLesson: SchoolLesson?, activity: Activity?, mode: Mode)
    {
        if (schoolLesson == null)
        {
            longName = longName
            color = color
        }
        else
        {
            longName = schoolLesson.longName
            color = schoolLesson.color

            if (mode != Mode.Display) binding.root.setOnClickListener {
                if (mode == Mode.EditOnClick)
                {
                    val intent = Intent(context, LessonEditActivity::class.java).apply {
                        putExtra("school_lesson_id", schoolLesson.shortName)
                    }

                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(App.littleVibrationEffect)
                    context.startActivity(intent)
                }
                else
                {
                    activity?.let {
                        it.setResult(0, Intent().putExtra("school_lesson_id", schoolLesson.shortName))
                        it.finish()
                    }
                }
            }
        }
    }
}
