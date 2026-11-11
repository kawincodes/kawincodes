package com.krstech.chesshelp.overlay

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.LinearLayout
import kotlin.math.min

/**
 * Root view of a floating window. Reports configuration changes of its own window (rotation, screen
 * resize), which can arrive at a different moment than the owning service hears about them.
 */
internal class OverlayRootLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    var onConfigurationChangedListener: (() -> Unit)? = null

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onConfigurationChangedListener?.invoke()
    }

    // Touch listeners on this view report taps through performClick(), which keeps accessibility
    // services able to trigger the same action.
    override fun performClick(): Boolean = super.performClick()
}

/** A vertical LinearLayout that never grows beyond [maxHeightPx]; weighted children shrink to fit. */
internal class MaxHeightLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    /** 0 = unlimited. */
    var maxHeightPx: Int = 0
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
            }
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, cappedHeightSpec(heightMeasureSpec, maxHeightPx))
    }

    private fun cappedHeightSpec(spec: Int, cap: Int): Int {
        if (cap <= 0) return spec
        val size = MeasureSpec.getSize(spec)
        return when (MeasureSpec.getMode(spec)) {
            MeasureSpec.UNSPECIFIED -> MeasureSpec.makeMeasureSpec(cap, MeasureSpec.AT_MOST)
            MeasureSpec.EXACTLY -> MeasureSpec.makeMeasureSpec(min(size, cap), MeasureSpec.EXACTLY)
            else -> MeasureSpec.makeMeasureSpec(min(size, cap), MeasureSpec.AT_MOST)
        }
    }
}
