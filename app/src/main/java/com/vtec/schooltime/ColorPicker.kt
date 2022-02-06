package com.vtec.schooltime

import android.content.Context
import android.graphics.Color
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.vtec.schooltime.databinding.ColorPickerBinding

internal interface ColorPicker : ColorPickerSeekBar.OnSeekBarChangeListener {
    val colorPickerBinding: ColorPickerBinding
    val context: Context

    fun setCardBackgroundColor(color: Int)

    fun setHexColorEditText(color: Int) {
        colorPickerBinding.hexColorEdit.setText("%02X%02X%02X".format(Color.red(color), Color.green(color), Color.blue(color)))
    }

    fun setSlidersProgress(color: Int) {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        colorPickerBinding.hueSlider.field = hsv[0] / 360
        colorPickerBinding.hueSlider.saturation = hsv[1]
        colorPickerBinding.hueSlider.value = hsv[2]

        colorPickerBinding.saturationSlider.hue = hsv[0]
        colorPickerBinding.saturationSlider.field = hsv[1]
        colorPickerBinding.saturationSlider.value = hsv[2]

        colorPickerBinding.valueSlider.hue = hsv[0]
        colorPickerBinding.valueSlider.saturation  = hsv[1]
        colorPickerBinding.valueSlider.field = hsv[2]
    }

    fun validateHex(text: CharSequence): Boolean {
        var validHex = true
        if (text.isNotEmpty())
        {
            for (char in text)
            {
                if (!char.isDigit() && char.uppercaseChar() !in 'A'..'F')
                {
                    validHex = false
                    break
                }
            }
        }
        colorPickerBinding.hexColorEdit.error = if (validHex) null else context.getString(R.string.invalid_hex)
        return validHex
    }

    fun setupColorPickerUI() {
        colorPickerBinding.colorWheel.setOnClickListener {
            ColorPickerDialog.Builder(context)
                .setTitle(context.getString(R.string.color_wheel))
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(context.getString(R.string.select),
                    object : ColorEnvelopeListener {
                        override fun onColorSelected(envelope: ColorEnvelope, fromUser: Boolean) {
                            setCardBackgroundColor(envelope.color)
                            setHexColorEditText(envelope.color)
                            setSlidersProgress(envelope.color)
                        }
                    })
                .setNegativeButton(context.getString(R.string.cancel)) { dialogInterface, i -> dialogInterface.dismiss() }
                .attachAlphaSlideBar(false) // the default value is true.
                .attachBrightnessSlideBar(true) // the default value is true.
                .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                .show()
        }

        colorPickerBinding.hexColorEdit.doOnTextChanged { text, start, before, count ->
            if (text != null) validateHex(text)
        }

        colorPickerBinding.hexColorEdit.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(textView: TextView, actionId: Int, keyEvent: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || keyEvent?.action == KeyEvent.ACTION_DOWN && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (validateHex(textView.text))
                    {
                        val string = "#" + textView.text.toString().padEnd(6, '0')
                        val color = Color.parseColor(string)
                        setCardBackgroundColor(color)
                        setHexColorEditText(color)
                        setSlidersProgress(color)

                        // occult keyboard
                        val inputMethodManager = context.getSystemService(InputMethodManager::class.java)
                        inputMethodManager.hideSoftInputFromWindow(colorPickerBinding.hexColorEdit.windowToken, 0)

                        return true
                    }
                }
                return false
            }
        })

        colorPickerBinding.hueSlider.onSeekBarChangeListener = this
        colorPickerBinding.saturationSlider.onSeekBarChangeListener = this
        colorPickerBinding.valueSlider.onSeekBarChangeListener = this
    }

    override fun onSeekBarChanged(seekBar: ColorPickerSeekBar) {
        var hsv = FloatArray(3)
        when (seekBar.id)
        {
            R.id.hue_slider -> {
                hsv = floatArrayOf(seekBar.field * 360, colorPickerBinding.saturationSlider.field, colorPickerBinding.valueSlider.field)
                colorPickerBinding.saturationSlider.hue = seekBar.field * 360
                colorPickerBinding.valueSlider.hue = seekBar.field * 360
            }
            R.id.saturation_slider ->
            {
                hsv = floatArrayOf(colorPickerBinding.hueSlider.field * 360, seekBar.field, colorPickerBinding.valueSlider.field)
                colorPickerBinding.hueSlider.saturation = seekBar.field
                colorPickerBinding.valueSlider.saturation = seekBar.field
            }
            R.id.value_slider -> {
                hsv = floatArrayOf(colorPickerBinding.hueSlider.field * 360, colorPickerBinding.saturationSlider.field, seekBar.field)
                colorPickerBinding.hueSlider.value = seekBar.field
                colorPickerBinding.saturationSlider.value = seekBar.field
            }
        }
        val color = Color.HSVToColor(hsv)
        setCardBackgroundColor(color)
        setHexColorEditText(color)
    }
}