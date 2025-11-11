package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.TestRectangle
import ch.scorpion.jabbah.draw.graphics.Color
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