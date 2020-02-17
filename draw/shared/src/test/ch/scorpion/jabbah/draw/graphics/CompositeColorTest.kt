package ch.scorpion.jabbah.draw.graphics

import kotlin.test.Test
import kotlin.test.assertEquals

/** Unit tests for [CompositeColor].*/
class CompositeColorTest {

    @Test
    fun shouldCalculateDisableTextColor() {
        assertEquals(Color(128, 128, 128), CompositeColor(backgroundColor = Color.BLACK, textColor = Color.WHITE).disabledTextColor)
        assertEquals(Color(128, 128, 128), CompositeColor(backgroundColor = Color.WHITE, textColor = Color.BLACK).disabledTextColor)
    }

	@Test
	fun shouldDeriveBackgroundTowardsForegroundColor() {
		assertEquals(Color(32, 32, 32), CompositeColor(Color.WHITE, Color.BLACK).deriveBackgroundTowardsForegroundColor().backgroundColor)
	}

	@Test
	fun shouldDeriveBackgroundTowardsTextColor() {
		assertEquals(Color(32, 32, 32), CompositeColor(Color.WHITE, Color.BLACK, Color.WHITE).deriveBackgroundTowardsTextColor().backgroundColor)
	}
}