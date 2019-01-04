package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.Math

/**
 * Represents a color with RGB and alpha channel values.
 */
data class Color(val red: Int, val green: Int, val blue: Int, val alpha: Int) {

    companion object {
        private const val FACTOR = 0.7

        val BLACK = Color(0, 0, 0)
        val WHITE = Color(255, 255, 255)
        val RED = Color(255, 0, 0)
        val ORANGE = Color(255, 200, 0)
        val DARK_GRAY = Color(64, 64, 64)
        val GRAY = Color(128, 128, 128)
        val LIGHT_GRAY = Color(192, 192, 192)
        val BLUE = Color(0, 0, 255)
        val YELLOW = Color(255, 255, 0)
	    val GREEN = Color(18, 138, 58)
    }

    constructor(red: Int, green: Int, blue: Int) : this(red, green, blue, 255)

    constructor(color: Color, alpha: Int) : this(color.red, color.green, color.blue, alpha)

    fun darker(): Color {
        return Color(
            Math.max((red * FACTOR).toInt(), 0),
            Math.max((green * FACTOR).toInt(), 0),
            Math.max((blue * FACTOR).toInt(), 0)
        )
    }

	fun brighter(): Color {
		return return Color(
			Math.min((red / FACTOR).toInt(), 255),
			Math.min((green / FACTOR).toInt(), 255),
			Math.min((blue / FACTOR).toInt(), 255)
		)
	}

    /** Returns a new [Color] with the same RGB values as this [Color], but with the specified alpha value.*/
    fun withAlpha(alpha: Int): Color {
        return Color(red, green, blue, alpha)
    }
}