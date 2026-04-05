package io.antarescircuit.jabbah.draw.view

import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawTestRule
import io.antarescircuit.jabbah.draw.InputEventContext
import io.antarescircuit.jabbah.draw.View
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [InvalidatableViewPainter].
 */
class InvalidatableViewPainterTest {

    private var view: View<InputEventContext> = mock(MockMode.autofill)
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