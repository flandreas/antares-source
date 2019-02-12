package ch.scorpion.jabbah.draw.graphics

import kotlin.math.max
import kotlin.math.min

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
			max((red * FACTOR).toInt(), 0),
			max((green * FACTOR).toInt(), 0),
			max((blue * FACTOR).toInt(), 0))
	}

	fun brighter(): Color {
		return Color(
			min((red / FACTOR).toInt(), 255),
			min((green / FACTOR).toInt(), 255),
			min((blue / FACTOR).toInt(), 255))
	}

	fun between(other: Color): Color {
		return Color(
			(red + other.red) / 2,
			(green + other.green) / 2,
			(blue + other.blue) / 2)
	}

	/** Returns a new [Color] with the same RGB values as this [Color], but with the specified alpha value.*/
	fun withAlpha(alpha: Int): Color {
		return Color(red, green, blue, alpha)
	}
}