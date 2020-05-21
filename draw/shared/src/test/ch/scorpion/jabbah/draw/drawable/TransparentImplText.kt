package ch.scorpion.jabbah.draw.drawable

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.TestRectangle
import ch.scorpion.jabbah.draw.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class TransparentImplText {

	companion object {
		init {
			DrawTestRule.configure()
		}
	}

	private val transparent = TransparentImpl(TestRectangle())

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