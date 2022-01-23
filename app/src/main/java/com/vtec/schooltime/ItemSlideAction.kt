package com.vtec.schooltime

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.graphics.toRectF
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.tanh

class ItemSlideAction(private val context: Context, private val editIcon: Drawable, private val deleteAction: Boolean, private val action: (Int) -> Unit, private val onMoveEvent: ((recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) -> Boolean)?) : ItemTouchHelper.Callback() {
    private var slideActionState = false
    private var slideComplete = false
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private val baseBgColor = context.getColor(R.color.app_bg2)

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(if (onMoveEvent != null) ItemTouchHelper.UP or ItemTouchHelper.DOWN else 0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return onMoveEvent?.invoke(recyclerView, viewHolder, target) ?: false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = Float.NaN

    override fun getSwipeEscapeVelocity(defaultValue: Float) = Float.NaN

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val slideWindowSize = (78 * context.resources.displayMetrics.density).toInt()
        val animationSlideWindowSize = (slideWindowSize * 2)
        val newDX = tanh(dX / (animationSlideWindowSize * 3)) * animationSlideWindowSize
        if (dX != 0f)
        {
            val rect = Rect(itemView.left, itemView.top, itemView.right, itemView.bottom)
            val cardCornerSize = if (itemView is CardView) itemView.radius else 0F

            val fractionOfCompletion = min(abs(newDX) / slideWindowSize, 1f)
            val fractionOfCompletionSquared = fractionOfCompletion * fractionOfCompletion
            val animatedBgColor = if (!deleteAction) baseBgColor else getColorTransitionState(baseBgColor, context.getColor(R.color.delete_red), fractionOfCompletionSquared)
            val bgPaint = Paint().apply { color = animatedBgColor }
            c.drawRoundRect(rect.toRectF(), cardCornerSize, cardCornerSize, bgPaint)

            val verticalCenter = itemView.top + (itemView.bottom - itemView.top) / 2

            if (fractionOfCompletion == 1f)
            {
                if (!slideComplete)
                {
                    slideComplete = true
                    vibrator.vibrate(App.littleVibrationEffect)
                }
            }
            else
                slideComplete = false

            val marginCoeff = 0.5/(fractionOfCompletion * 0.5) * context.resources.displayMetrics.density
            val horizontalMargin = (23 * marginCoeff).toInt()
            val verticalMargin = (10 * marginCoeff).toInt()
            val horizontalSize = slideWindowSize - 2 * horizontalMargin
            val verticalSize = itemView.height - 2 * verticalMargin
            val size = min(horizontalSize, verticalSize)
            val top = verticalCenter - size / 2

            if (dX > 0)
                editIcon.setBounds(itemView.left + horizontalMargin, top, itemView.left + horizontalMargin + size, top + size)
            else
                editIcon.setBounds(itemView.right - horizontalMargin - size, top, itemView.right - horizontalMargin, top + size)

            editIcon.alpha = (fractionOfCompletionSquared * 255).toInt()
            editIcon.colorFilter = BlendModeColorFilter(getContrastingColor(animatedBgColor), BlendMode.SRC_ATOP)
            editIcon.draw(c)
        }
        else
        {
            slideActionState = false
        }

        if (abs(newDX) > slideWindowSize && !isCurrentlyActive && !slideActionState)
        {
            slideActionState = true
            action(viewHolder.adapterPosition)
        }
        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            newDX,
            dY,
            actionState,
            isCurrentlyActive
        )
    }
}
