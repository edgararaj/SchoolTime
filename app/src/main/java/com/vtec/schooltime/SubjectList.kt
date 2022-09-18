package com.vtec.schooltime

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vtec.schooltime.activities.SubjectEditActivity
import com.vtec.schooltime.databinding.UniversalCardBinding

class SubjectListAdapter(private val schoolSubjects: SchoolSubjects, private val onSelectAndFinishActivity: ((String) -> Unit)?, private val mode: SubjectVH.Mode) : RecyclerView.Adapter<SubjectVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectVH {
        val binding = UniversalCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubjectVH(binding)
    }

    override fun onBindViewHolder(holder: SubjectVH, position: Int) {
        schoolSubjects.toList()[position].second.let {
            holder.bind(it, onSelectAndFinishActivity, mode)
        }
    }

    override fun getItemCount() = schoolSubjects.size
}

class SubjectVH(private val binding: UniversalCardBinding) : RecyclerView.ViewHolder(binding.root) {
    private val context: Context = binding.root.context

    var color: Int = Color.BLACK
        set(value) {
            binding.root.setCardBackgroundColor(value)

            val contrastyFgColor = getContrastingColor(value)
            binding.longName.setTextColor(contrastyFgColor)

            binding.shortName.setTextColor(value)
            binding.badge.setCardBackgroundColor(contrastyFgColor)

            field = value
        }

    var longName: String = context.getString(R.string.unnamed)
        set(value) {
            binding.longName.text = value
            field = value
        }

    var shortName: String = context.getString(R.string.unnamed_small)
        set(value) {
            binding.shortName.text = value
            field = value
        }

    enum class Mode {
        Display, EditOnClick, SelectAndFinishActivity
    }

    fun bind(schoolSubject: SchoolSubject?, onSelectAndFinishActivity: ((String) -> Unit)?, mode: Mode)
    {
        if (schoolSubject == null)
        {
            longName = longName
            shortName = shortName
            color = color
        }
        else
        {
            longName = schoolSubject.longName
            shortName = schoolSubject.shortName
            color = schoolSubject.color

            if (mode != Mode.Display) binding.root.setOnClickListener {
                if (mode == Mode.EditOnClick)
                {
                    val intent = Intent(context, SubjectEditActivity::class.java).apply {
                        putExtra("school_subject_id", schoolSubject.shortName)
                    }

                    val vibrator = context.getSystemService(Vibrator::class.java)
                    vibrator.vibrate(App.littleVibrationEffect)
                    context.startActivity(intent)
                }
                else
                {
                    if (onSelectAndFinishActivity != null) onSelectAndFinishActivity(schoolSubject.shortName)
                }
            }
        }
    }
}
