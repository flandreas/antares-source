package ch.scorpion.antares.view.output

import ch.scorpion.jabbah.draw.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class LightColorTest {

	@Test
	fun shouldCreateGradient() {
		assertEquals(LightColor.WHITE.offColor, LightColor.WHITE.gradient(0.0f))
		assertEquals(Color(133, 133, 133), LightColor.WHITE.gradient(0.5f))
		assertEquals(LightColor.WHITE.onColor, LightColor.WHITE.gradient(1.0f))
	}
}