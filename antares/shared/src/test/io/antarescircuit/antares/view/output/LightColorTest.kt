package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.jabbah.draw.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class LightColorTest {

	init {
		AntaresTestRule.configure()
	}

	@Test
	fun shouldCreateGradient() {
		assertEquals(LightColor.WHITE.offColor, LightColor.WHITE.gradient.at(0.0f))
		assertEquals(Color(160, 160, 160), LightColor.WHITE.gradient.at(0.5f))
		assertEquals(LightColor.WHITE.onColor, LightColor.WHITE.gradient.at(1.0f))
	}
}