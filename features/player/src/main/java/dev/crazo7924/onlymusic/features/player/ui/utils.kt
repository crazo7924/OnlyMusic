package dev.crazo7924.onlymusic.features.player.ui

import android.util.Log
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

internal fun Long.toTimeString(): String {
    var hours = 0L
    var minutes = 0L
    var seconds = this / 1000
    if (seconds >= 60) {
        minutes = seconds / 60
        seconds %= 60
    }

    if (minutes >= 60) {
        hours = minutes / 60
        minutes %= 60
    }

    val hh = if (hours >= 10) "$hours" else "0$hours"
    val mm = if (minutes >= 10) "$minutes" else "0$minutes"
    val ss = if (seconds >= 10) "$seconds" else "0$seconds"

    if (hh == "00") {
        return "$mm:$ss"
    }
    return "$hh:$mm:$ss"
}

internal val CenteredSquareShape: Shape = object : Shape {
    val TAG = "CenteredSquareShape"
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {

        Log.d(TAG, "size: $size")
        val minSide = minOf(size.width, size.height)
        val maxSide = maxOf(size.width, size.height)
        val rect = RoundRect(
            (maxSide - minSide) / 2,
            0F,
            (maxSide - minSide) / 2 + minSide,
            minSide,
            cornerRadius = CornerRadius(16f)
        )
        Log.d(TAG, "rect: $rect")
        return Outline.Rounded(rect)
    }

    override fun toString(): String = TAG
}