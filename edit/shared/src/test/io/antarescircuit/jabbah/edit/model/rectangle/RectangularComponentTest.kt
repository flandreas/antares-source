package io.antarescircuit.jabbah.edit.model.rectangle

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.edit.EditTestRule
import io.antarescircuit.jabbah.draw.DrawableAdapter
import io.antarescircuit.jabbah.draw.DrawableEvent
import io.antarescircuit.jabbah.edit.StyleProviderMockBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [RectangularComponent].
 */
class RectangularComponentTest {

	@BeforeTest
	fun setup() {
		EditTestRule.configure()
	}

    @Test
    fun shouldReturnBounds() {
        val rect = RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(10, 20, 30, 40))
        assertEquals(10.0, rect.shape.x)
        assertEquals(20.0, rect.shape.y)
        assertEquals(30.0, rect.shape.width)
        assertEquals(40.0, rect.shape.height)
    }

    @Test
    fun shouldSetFrame() {
        val rect = RectangleComponent(styleProvider = StyleProviderMockBuilder().build())
        rect.setFrame(1.0, 2.0, 3.0, 4.0)
        assertEquals(1.0, rect.x)
        assertEquals(2.0, rect.y)
        assertEquals(3.0, rect.width)
        assertEquals(4.0, rect.height)
    }

    @Test
    fun shouldSetLocation() {
        val rect = RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(10, 20, 30, 40))
        rect.location = Point2D(100, 200)
        assertEquals(100.0, rect.x)
        assertEquals(200.0, rect.y)
        assertEquals(30.0, rect.width)
        assertEquals(40.0, rect.height)
    }

    @Test
    fun shouldCalculateBoundingBox() {
        val rect = RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(10, 20, 30, 40))
        assertEquals(9.0, rect.boundingBox.x)
        assertEquals(19.0, rect.boundingBox.y)
        assertEquals(32.0, rect.boundingBox.width)
        assertEquals(42.0, rect.boundingBox.height)
    }

    @Test
    fun shouldMirrorHorizontally() {
        val rect = RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(10, 20, 30, 40))
        rect.mirrorHorizontally(0.0)
        assertEquals(-40.0, rect.x)
        assertEquals(20.0, rect.y)
        assertEquals(30.0, rect.width)
        assertEquals(40.0, rect.height)
    }

    @Test
    fun shouldMirrorHVertically() {
        val rect = RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(10, 20, 30, 40))
        rect.mirrorVertically(0.0)
        assertEquals(10.0, rect.x)
        assertEquals(-60.0, rect.y)
        assertEquals(30.0, rect.width)
        assertEquals(40.0, rect.height)
    }

    @Test
    fun shouldSnapX() {
        val rect = RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(10, 20, 30, 40))
	    assertTrue(rect.snappableX.map { it.x }.toList().containsAll(listOf(10.0, 25.0, 40.0)))
    }

    @Test
    fun shouldSnapY() {
        val rect = RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(10, 20, 30, 40))
	    assertTrue(rect.snappableY.map { it.y }.toList().containsAll(listOf(20.0, 40.0, 60.0)))
    }

    @Test
    fun shouldInvalidateWhenSettingFrame() {
        val rect = RectangleComponent(100.0, 100.0, 200.0, 100.0)
        var invalidated = false
        rect.addDrawableListener(object : DrawableAdapter() {
            override fun drawableInvalidated(event: DrawableEvent) {
                invalidated = true
            }
        })
        rect.setFrame(0.0, 0.0, 20.0, 20.0)
        assertEquals(invalidated, true)

        invalidated = false
        rect.setFrame(Rectangle2D(1.0, 2.0, 3.0, 4.0))
        assertEquals(invalidated, true)
    }
}