package com.vtec.schooltime

import android.view.animation.Interpolator
import kotlin.math.abs

class ReverseInterpolator : Interpolator {
    override fun getInterpolation(input: Float) = abs(input - 1)
}

class NormalInterpolator : Interpolator {
    override fun getInterpolation(input: Float) = input
}
