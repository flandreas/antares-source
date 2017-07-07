package ch.scorpion.jabbah.draw.view

import com.nhaarman.mockito_kotlin.mock
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

/**
 * Unit tests for [InvalidatableViewPainter].
 */
class InvalidatableViewPainterTest {

    private var view: View<InputEventContext> = mock()
    private var viewPainter = InvalidatableViewPainter(view)

    @Before
    fun setup() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldSumInvalidAreas() {
        viewPainter.invalidateRegion(Rectangle2D(100.0, 100.0, 10.0, 10.0), false)
        viewPainter.invalidateRegion(Rectangle2D(200.0, 200.0, 10.0, 10.0), false)
        assertEquals(Rectangle2D(100.0, 100.0, 110.0, 110.0), viewPainter.dirtyRegion)
    }
}