package com.krstech.chesshelp.capture

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import androidx.annotation.RequiresApi

internal class ScreenSpec(val size: PixelSize, val densityDpi: Int)

/** Reads the real size of the default display in its current rotation, in physical pixels. */
internal object ScreenMetrics {

    fun read(context: Context): ScreenSpec {
        val fallback = context.resources.displayMetrics
        val density = if (fallback.densityDpi > 0) fallback.densityDpi else DisplayMetrics.DENSITY_DEFAULT
        val size = windowMetricsSize(context)
            ?: legacyRealSize(context)
            ?: PixelSize(fallback.widthPixels, fallback.heightPixels)
        return ScreenSpec(size, density)
    }

    /**
     * The maximum window metrics rather than the current ones: this is read through a non-activity context,
     * whose "current" bounds can be those of one of the app's own activities in split-screen, while the
     * maximum bounds are the display.
     */
    private fun windowMetricsSize(context: Context): PixelSize? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        return try {
            Api30.maximumWindowSize(context)?.takeIf { it.isValid }
        } catch (e: RuntimeException) {
            null
        }
    }

    @Suppress("DEPRECATION") // getRealMetrics: the only source below API 30, and the fallback above it.
    private fun legacyRealSize(context: Context): PixelSize? {
        return try {
            val display = context.getSystemService(DisplayManager::class.java)
                ?.getDisplay(Display.DEFAULT_DISPLAY) ?: return null
            val metrics = DisplayMetrics()
            display.getRealMetrics(metrics)
            PixelSize(metrics.widthPixels, metrics.heightPixels).takeIf { it.isValid }
        } catch (e: RuntimeException) {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private object Api30 {
        fun maximumWindowSize(context: Context): PixelSize? {
            val bounds = context.getSystemService(WindowManager::class.java)?.maximumWindowMetrics?.bounds
                ?: return null
            return PixelSize(bounds.width(), bounds.height())
        }
    }
}
