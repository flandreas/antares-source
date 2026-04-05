package io.antarescircuit.jabbah.draw.graphics

import kotlin.test.Test
import kotlin.test.assertEquals

class ColorGradientTest {

	@Test
	fun shouldAccessGradient() {
		val gradient = ColorGradient(Color(8, 8, 8), Color.WHITE, 100)

		assertEquals(Color(8, 8, 8), gradient.at(0.0f))
		assertEquals(Color(133, 133, 133), gradient.at(0.5f))
		assertEquals(Color.WHITE, gradient.at(1.0f))
	}
}