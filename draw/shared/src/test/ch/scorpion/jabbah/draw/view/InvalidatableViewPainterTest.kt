package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [InvalidatableViewPainter].
 */
class InvalidatableViewPainterTest {

    private var view: View<InputEventContext> = mockk()
    private var viewPainter = InvalidatableViewPainter(view)

	@BeforeTest
	fun beforeTest() {
		DrawTestRule.configure()
	}

    @Test
    fun shouldSumInvalidAreas() {
        viewPainter.invalidateRegion(Rectangle2D(100.0, 100.0, 10.0, 10.0))
        viewPainter.invalidateRegion(Rectangle2D(200.0, 200.0, 10.0, 10.0))
        assertEquals(Rectangle2D(100.0, 100.0, 110.0, 110.0), viewPainter.dirtyRegion)
    }
}