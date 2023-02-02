package ch.scorpion.jabbah.draw.graphics

import kotlin.math.roundToInt

class ColorGradient(val start: Color, val end: Color, val steps: Int = 100) {

	private val colors: List<Color> by lazy { createGradient() }

	fun at(x: Float): Color =
		colors[(x.coerceIn(0.0f..1.0f) * steps).roundToInt().coerceIn(0 until steps)]

	private fun createGradient(): List<Color> {
		val result = mutableListOf<Color>()
		val dRed = (end.red - start.red).toFloat() / (steps - 1)
		val dGreen = (end.green - start.green).toFloat() / (steps - 1)
		val dBlue = (end.blue - start.blue).toFloat() / (steps - 1)
		var red = start.red.toFloat()
		var green = start.green.toFloat()
		var blue = start.blue.toFloat()
		for (i in 0 until steps) {
			result.add(Color(
				red.roundToInt().coerceIn(0..255),
				green.roundToInt().coerceIn(0..255),
				blue.roundToInt().coerceIn(0..255)))
			red += dRed
			green += dGreen
			blue += dBlue
		}
		return result
	}
}