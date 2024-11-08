package ch.scorpion.jabbah.draw.graphics

import kotlin.math.roundToInt

class ColorGradient(val start: Color, val end: Color, val steps: Int = 100) {

	companion object {

		fun calculateAt(start: Color, end: Color, steps: Int = 100, at: Int): Color {
			val dRed = (end.red - start.red).toFloat() / (steps - 1)
			val dGreen = (end.green - start.green).toFloat() / (steps - 1)
			val dBlue = (end.blue - start.blue).toFloat() / (steps - 1)
			val red = start.red.toFloat() + dRed * at
			val green = start.green.toFloat() + dGreen * at
			val blue = start.blue.toFloat() + dBlue * at
			return Color(
				red.roundToInt().coerceIn(0..255),
				green.roundToInt().coerceIn(0..255),
				blue.roundToInt().coerceIn(0..255)
			)
		}
	}

	val colors: List<Color> by lazy { createGradient() }

	fun at(x: Float): Color =
		colors[(x.coerceIn(0.0f..1.0f) * steps).roundToInt().coerceIn(0 until steps)]

	private fun createGradient(): List<Color> {
		val result = mutableListOf<Color>()
		for (i in 0 until steps) {
			result.add(calculateAt(start, end, steps, i))
		}
		return result
	}
}

class CompositeColorGradient(val background: Color, val start: Color, val end: Color, val steps: Int = 100) {

	private val colors: List<CompositeColor> by lazy { createGradient() }

	fun at(x: Float): CompositeColor =
		colors[(x.coerceIn(0.0f..1.0f) * steps).roundToInt().coerceIn(0 until steps)]

	private fun createGradient(): List<CompositeColor> {
		return ColorGradient(start, end, steps)
			.colors
			.map { CompositeColor(it, background) }
			.toList()
	}
}