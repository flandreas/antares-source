package io.antarescircuit.jabbah.draw.drawable

import io.antarescircuit.jabbah.draw.DrawTestRule
import io.antarescircuit.jabbah.draw.TestRectangle
import io.antarescircuit.jabbah.draw.graphics.Color
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TransparentImplText {

	private val transparent = TransparentImpl(TestRectangle())

	@BeforeTest
	fun setup() {
		DrawTestRule.configure()
	}

	@Test
	fun fullOpacityShouldNotAlterColor() {
		transparent.transparency = Transparent.FULLY_OPAQUE

		assertEquals(Color.RED, transparent.applyTo(Color.RED))
	}

	@Test
	fun shouldApplyTransparencyToOpaqueColor() {
		transparent.transparency = 127

		assertEquals(Color.WHITE.withAlpha(127), transparent.applyTo(Color.WHITE))
	}

	@Test
	fun shouldNotIncreaseOpacityOfTransparentColor() {
		transparent.transparency = Transparent.FULLY_OPAQUE

		assertEquals(Color.WHITE.withAlpha(0), transparent.applyTo(Color.WHITE.withAlpha(0)))
	}

	@Test
	fun shouldReduceTransparencyOfSemiTransparentColor() {
		transparent.transparency = 127

		assertEquals(Color.WHITE.withAlpha(63), transparent.applyTo(Color.WHITE.withAlpha(127)))
	}
}