package ch.scorpion.jabbah.base.math

fun interpolateInt(x0: Int, x1: Int, y1: Int, x2: Int, y2: Int): Int {
    val m = (y2 - y1).toFloat() / (x2 - x1)
    return y1 + (m * (x0 - x1)).toInt()
}

fun IntRange.interpolate(x0: Int, y1: Int, y2: Int): Int =
    interpolateInt(x0, start, y1, endInclusive, y2)