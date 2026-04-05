package io.antarescircuit.jabbah.base.geom

import kotlin.math.max
import kotlin.math.min

data class Margin(
    val top: Int,
    val left: Int,
    val bottom: Int,
    val right: Int
) {
    companion object {

        val NONE = Margin(0, 0, 0, 0)

        fun allOf(value: Int) = Margin(top = value, left = value, bottom = value, right = value)
    }

    val horizontalSum = left + right
    val verticalSum = top + bottom

    fun <T: MutableRectangularShape> reduce(rect: T): T {
        val x0: Double = min(rect.minX + top, rect.maxX)
        val x1: Double = max(x0, rect.maxX - right)
        val y0: Double = min(rect.minY + top, rect.maxY)
        val y1: Double = max(y0, rect.maxY - bottom)
        rect.setFrame(x0, y0, x1 - x0, y1 - y0)
        return rect
    }
}
