package com.vtec.schooltime

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatSeekBar

/**
 * Created by Patrick Geselbracht on 2017-03-04
 *
 * @author Patrick Geselbracht <github></github>@pattafeufeu.de>
 * @since v1.1.0
 */
@RequiresApi(Build.VERSION_CODES.Q)
internal class ColorPickerSeekBar : AppCompatSeekBar {
    private lateinit var textPaint: Paint
    private val textRect = Rect()

    @ColorInt
    private var textColor = 0

    @Dimension(unit = 2)
    private var textSize = 0f
    private var text: String? = null

    constructor(context: Context?) : super(context!!) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        max = 255

        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.ColorPickerSeekBar
            )
            textColor = typedArray.getColor(R.styleable.ColorPickerSeekBar_android_textColor, -0x1000000)
            textSize = typedArray.getDimension(
                R.styleable.ColorPickerSeekBar_android_textSize,
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    18f,
                    resources.displayMetrics
                )
            )
            text = typedArray.getString(R.styleable.ColorPickerSeekBar_android_text)
            typedArray.recycle()
        }

        textPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        textPaint.color = textColor
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.textSize = textSize
        textPaint.textAlign = Paint.Align.CENTER

        val colorFilter = BlendModeColorFilter(textColor, BlendMode.SRC_ATOP)
        thumb.colorFilter = colorFilter
        progressDrawable.colorFilter = colorFilter

        /* Measures 255 instead of the actual text because otherwise the padding would jump up
         * and down each time the text with its ascender and descenders changes.
         *
         * --
         *
         * Since we're only interested in a roundabout height depending on the text's font size
         * anyway, calculating the text bounds of this value is enough in this case.
         */
        textPaint.getTextBounds("255", 0, 3, textRect)
        val morePadding = (10 * resources.displayMetrics.density).toInt()
        setPadding(
            paddingLeft, textRect.height() + thumb.intrinsicHeight / 2 + morePadding,
            paddingRight, paddingBottom + morePadding
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawText(
            (if (text == null) progress.toString() else text)!!, (
                    thumb.bounds.left + paddingLeft).toFloat(), (
                    textRect.height() + (paddingTop shr 2)).toFloat(),
            textPaint
        )
    }
}