package com.vtec.schooltime

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.core.math.MathUtils
import kotlin.math.*

internal class ColorPickerSeekBar(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val textPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
    private val textRect = Rect()
    private val hueShaderPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private lateinit var alphaBar: Bitmap

    private val bigThumbRadius = 12 * resources.displayMetrics.density
    private val smallThumbRadius = 8 * resources.displayMetrics.density
    private var thumbRadius = smallThumbRadius
    private var thumbRadiusAnimator: Animator

    private val bigTextSize = 22 * resources.displayMetrics.scaledDensity
    private val smallTextSize = 18 * resources.displayMetrics.scaledDensity
    private var textSize = smallTextSize
    private var textSizeAnimator: Animator

    private val bigTrackStrokeWidth = 17 * resources.displayMetrics.density
    private val smallTrackStrokeWidth = 10 * resources.displayMetrics.density
    private var trackStrokeWidth = smallTrackStrokeWidth
    private var trackStrokeWidthAnimator: Animator

    private var mTouchThumbOffset = 0f
    private var isDragging = false
    private var dragX = 0

    private var max = 255
    private val min = 0

    private lateinit var type: Type
    private lateinit var barColors: IntArray

    private val baseThumbStretch = 0
    private var thumbStretch = baseThumbStretch
        set(value) {
            field = value
            requestLayout()
        }
    private lateinit var thumbStretchAnimator: Animator

    private fun dimField(field: Float) = field / 1.4f + 0.2f
    private val trackY
        get() = height / 2f + bigThumbRadius

    private val availableWidth: Int
        get() = width - paddingLeft - paddingRight

    private var progress: Int = min
        set(value) {
            field = MathUtils.clamp(value, min, max)
            invalidate()
        }

    var field
        get() = (progress - min) / (max.toFloat() - min)
        set(value) {
            progress = (value * (max - min) + min).toInt()
        }

    var hue = 0f
        set(x) {
            field = x
            when (type)
            {
                Type.Saturation -> {
                    barColors[0] = Color.HSVToColor(floatArrayOf(hue, 0f, value))
                    barColors[1] = Color.HSVToColor(floatArrayOf(hue, 1f, value))
                }
                Type.Value -> {
                    barColors[0] = Color.HSVToColor(floatArrayOf(hue, saturation, dimField(0f)))
                    barColors[1] = Color.HSVToColor(floatArrayOf(hue, saturation, dimField(1f)))
                }
                else -> {}
            }
            invalidate()
        }

    var saturation = 0f
        set(x) {
            field = x
            when (type)
            {
                Type.Hue -> {
                    for (i in barColors.indices)
                        barColors[i] = Color.HSVToColor(floatArrayOf(i.toFloat(), saturation, value))
                }
                Type.Value -> {
                    barColors[0] = Color.HSVToColor(floatArrayOf(hue, saturation, dimField(0f)))
                    barColors[1] = Color.HSVToColor(floatArrayOf(hue, saturation, dimField(1f)))
                }
                else -> {}
            }
            invalidate()
        }

    var value = 0f
        set(x) {
            field = dimField(x)
            when (type)
            {
                Type.Hue -> {
                    for (i in barColors.indices)
                        barColors[i] = Color.HSVToColor(floatArrayOf(i.toFloat(), saturation, value))
                }
                Type.Saturation -> {
                    barColors[0] = Color.HSVToColor(floatArrayOf(hue, 0f, value))
                    barColors[1] = Color.HSVToColor(floatArrayOf(hue, 1f, value))
                }
                else -> {}
            }
            invalidate()
        }

    lateinit var onSeekBarChangeListener: (ColorPickerSeekBar) -> Unit

    enum class Type {
        Custom, Hue, Saturation, Value, Alpha
    }

    init {
        if (attrs != null && context != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerSeekBar)
            type = Type.values()[typedArray.getInt(R.styleable.ColorPickerSeekBar_type, Type.Custom.ordinal)]

            if (type == Type.Hue) {
                max = 360
                barColors = IntArray(360)
            }
            else
                barColors = IntArray(2)

            typedArray.recycle()
        }

        textPaint.textSize = bigTextSize
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.textAlign = Paint.Align.CENTER

        textPaint.getTextBounds("255", 0, 3, textRect)
        val morePadding = (2 * resources.displayMetrics.density).toInt()
        val horizontalPadding = max(bigThumbRadius.toInt() + morePadding, textRect.width() / 2)
        setPadding(horizontalPadding, 0, horizontalPadding,0)
        trackStrokeWidthAnimator = ValueAnimator.ofFloat(smallTrackStrokeWidth, bigTrackStrokeWidth).apply {
            duration = 50
            addUpdateListener {
                trackStrokeWidth = it.animatedValue as Float
                invalidate()
            }
        }
        thumbRadiusAnimator = ValueAnimator.ofFloat(smallThumbRadius, bigThumbRadius).apply {
            duration = 100
            addUpdateListener {
                thumbRadius = it.animatedValue as Float
                invalidate()
            }
        }
        textSizeAnimator = ValueAnimator.ofFloat(smallTextSize, bigTextSize).apply {
            duration = 50
            addUpdateListener {
                textSize = it.animatedValue as Float
                invalidate()
            }
        }

        if (context != null) {
            val drawable = AppCompatResources.getDrawable(context, R.drawable.alpha_bar)
            if (drawable != null) {
                alphaBar = drawable.toBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val padding = 10 * resources.displayMetrics.density
        setMeasuredDimension(
            resolveSizeAndState(width, widthMeasureSpec, 0),
            resolveSizeAndState(((bigThumbRadius + thumbStretch + padding) * 2 + textRect.height()).toInt(), heightMeasureSpec, 0)
        )
    }

    override fun onDraw(canvas: Canvas) {
        val x = paddingLeft + field * availableWidth
        if (type == Type.Alpha) {
            val path = Path().apply {
                addRoundRect(paddingLeft.toFloat() - trackStrokeWidth / 2, trackY - trackStrokeWidth / 2, (right - paddingRight).toFloat() + trackStrokeWidth / 2, trackY + trackStrokeWidth / 2, trackStrokeWidth / 2, trackStrokeWidth / 2, Path.Direction.CW)
            }
            canvas.save()
            canvas.clipPath(path)
            canvas.drawBitmap(alphaBar, paddingLeft.toFloat() - bigTrackStrokeWidth / 2, trackY - bigTrackStrokeWidth / 2, Paint())
            canvas.restore()
        } else {
            hueShaderPaint.shader = LinearGradient(0f, 0f, width.toFloat(), 0f, barColors, null, Shader.TileMode.CLAMP)
            hueShaderPaint.strokeWidth = trackStrokeWidth
            hueShaderPaint.strokeCap = Paint.Cap.ROUND
            canvas.drawLine(paddingLeft.toFloat(), trackY, (right - paddingRight).toFloat(), trackY, hueShaderPaint)
        }

        val color = when (type)
        {
            Type.Hue -> Color.HSVToColor(floatArrayOf(field * 360, saturation, value))
            Type.Saturation -> Color.HSVToColor(floatArrayOf(hue, field, value))
            Type.Value -> Color.HSVToColor(floatArrayOf(hue, saturation, dimField(field)))
            else -> context.getColor(R.color.app_fg)
        }

        textPaint.strokeCap = Paint.Cap.ROUND
        textPaint.strokeWidth = thumbRadius * 2
        textPaint.color = context.getColor(R.color.app_fg)
        canvas.drawLine(x, trackY - thumbStretch, x, trackY + thumbStretch, textPaint)

        textPaint.style = Paint.Style.FILL
        textPaint.textSize = textSize
        canvas.drawText(progress.toString(), x, trackY - thumbRadius - thumbStretch - textRect.height() / 2, textPaint)

        textPaint.color = color
        textPaint.strokeWidth -= if (isPressed) 8 else 15
        canvas.drawLine(x, trackY - thumbStretch, x, trackY + thumbStretch, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mTouchThumbOffset = field - (event.x - paddingLeft) / availableWidth
                if (abs(mTouchThumbOffset * availableWidth) > bigThumbRadius * 2) {
                    mTouchThumbOffset = 0f
                }
                startDrag(event)
            }
            MotionEvent.ACTION_MOVE -> if (isDragging) {
                trackTouchEvent(event)
            } else {
                startDrag(event)
            }
            MotionEvent.ACTION_UP -> {
                if (isDragging) {
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                    isPressed = false
                } else {
                    // Touch up when we never crossed the touch slop threshold should
                    // be interpreted as a tap-seek to that location.
                    onStartTrackingTouch()
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                }
                // ProgressBar doesn't know to repaint the thumb drawable
                // in its inactive state when the touch stops (because the
                // value has not apparently changed)
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    onStopTrackingTouch()
                    isPressed = false
                }
                invalidate() // see above explanation
            }
        }
        return true
    }

    private fun onStartTrackingTouch() {
        isDragging = true

        trackStrokeWidthAnimator.interpolator = NormalInterpolator()
        trackStrokeWidthAnimator.start()
        thumbRadiusAnimator.interpolator = NormalInterpolator()
        thumbRadiusAnimator.start()
        textSizeAnimator.interpolator = NormalInterpolator()
        textSizeAnimator.start()
    }

    private fun onStopTrackingTouch() {
        isDragging = false

        trackStrokeWidthAnimator.interpolator = ReverseInterpolator()
        trackStrokeWidthAnimator.start()
        thumbRadiusAnimator.interpolator = ReverseInterpolator()
        thumbRadiusAnimator.start()
        textSizeAnimator.interpolator = ReverseInterpolator()
        textSizeAnimator.start()

        thumbStretchAnimator = ValueAnimator.ofInt(thumbStretch, baseThumbStretch).apply {
            duration = 100
            addUpdateListener {
                thumbStretch = it.animatedValue as Int
                invalidate()
            }
            start()
        }
    }

    private fun startDrag(event: MotionEvent) {
        isPressed = true
        invalidate()

        onStartTrackingTouch()
        trackTouchEvent(event)
        parent?.requestDisallowInterceptTouchEvent(true)
    }

    private fun trackTouchEvent(event: MotionEvent) {
        val x = event.x.roundToInt()
        val y = event.y.roundToInt()
        val scale: Float
        val distance = abs(trackY - y)
        val sensitivity = -tanh((distance - 150) / 400.0) + 1
        val otherX: Int

        if (sensitivity > 1) {
            otherX = x
            dragX = x
        } else {
            otherX = (dragX + (x - dragX) * sensitivity).toInt()
        }

        if (layoutDirection == LAYOUT_DIRECTION_RTL) {
            if (x > width - paddingRight && sensitivity > 1) {
                scale = 0.0f
            } else if (x < paddingLeft && sensitivity > 1) {
                scale = 1.0f
            } else {
                scale = ((availableWidth - otherX + paddingLeft) / availableWidth.toFloat() + mTouchThumbOffset)
            }
        } else {
            if (x < paddingLeft && sensitivity > 1) {
                scale = 0.0f
            } else if (x > width - paddingRight && sensitivity > 1) {
                scale = 1.0f
            } else {
                scale = (otherX - paddingLeft) / availableWidth.toFloat() + mTouchThumbOffset
            }
        }

        val newThumbStretch = ((-8 / 0.5) * (min(1.0, sensitivity) - 1) + baseThumbStretch).toInt()
        if (newThumbStretch != thumbStretch) {
            thumbStretch = newThumbStretch
        }

        val newProgress = (scale * (max - min) + min).toInt()
        if (newProgress != progress) {
            progress = newProgress
            onSeekBarChangeListener(this)
        }
    }
}