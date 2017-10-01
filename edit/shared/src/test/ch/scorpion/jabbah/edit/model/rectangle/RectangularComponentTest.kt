package ch.scorpion.jabbah.edit.model.rectangle

import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.DrawableAdapter
import ch.scorpion.jabbah.draw.DrawableEvent
import ch.scorpion.jabbah.draw.style.StyleProviderMockBuilder
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [RectangularComponent].
 */
class RectangularComponentTest {

    companion object {
        @ClassRule @JvmField
        val drawTestRule = DrawTestRule()
    }

    @Test
    fun shouldReturnBounds() {
        val rect = RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(10, 20, 30, 40))
        assertThat(rect.shape.x, `is`(10.0))
        assertThat(rect.shape.y, `is`(20.0))
        assertThat(rect.shape.width, `is`(30.0))
        assertThat(rect.shape.height, `is`(40.0))
    }

    @Test
    fun shouldSetFrame() {
        val rect = RectangleComponent(styleProvider = StyleProviderMockBuilder().build())
        rect.setFrame(1.0, 2.0, 3.0, 4.0)
        assertThat(rect.x, `is`(1.0))
        assertThat(rect.y, `is`(2.0))
        assertThat(rect.width, `is`(3.0))
        assertThat(rect.height, `is`(4.0))
    }

    @Test
    fun shouldSetLocation() {
        val rect = RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(10, 20, 30, 40))
        rect.location = Point2D(100, 200)
        assertThat(rect.x, `is`(100.0))
        assertThat(rect.y, `is`(200.0))
        assertThat(rect.width, `is`(30.0))
        assertThat(rect.height, `is`(40.0))
    }

    @Test
    fun shouldCalculateBoundingBox() {
        val rect = RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(10, 20, 30, 40))
        assertThat(rect.boundingBox.x, `is`(9.0))
        assertThat(rect.boundingBox.y, `is`(19.0))
        assertThat(rect.boundingBox.width, `is`(32.0))
        assertThat(rect.boundingBox.height, `is`(42.0))
    }

    @Test
    fun shouldMirrorHorizontally() {
        val rect = RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(10, 20, 30, 40))
        rect.mirrorHorizontally(0.0)
        assertThat(rect.x, `is`(-40.0))
        assertThat(rect.y, `is`(20.0))
        assertThat(rect.width, `is`(30.0))
        assertThat(rect.height, `is`(40.0))
    }

    @Test
    fun shouldMirrorHVertically() {
        val rect = RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(10, 20, 30, 40))
        rect.mirrorVertically(0.0)
        assertThat(rect.x, `is`(10.0))
        assertThat(rect.y, `is`(-60.0))
        assertThat(rect.width, `is`(30.0))
        assertThat(rect.height, `is`(40.0))
    }

    @Test
    fun shouldSnapX() {
        val rect = RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(10, 20, 30, 40))
        assertThat(rect.snappableX.map { it.x }.toList(), `hasItems`(10.0, 25.0, 40.0))
    }

    @Test
    fun shouldSnapY() {
        val rect = RectangleComponent(styleProvider = StyleProviderMockBuilder().build(), shape = Rectangle2D(10, 20, 30, 40))
        assertThat(rect.snappableY.map { it.y }.toList(), `hasItems`(20.0, 40.0, 60.0))
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
        assertThat(invalidated, `is`(true))

        invalidated = false
        rect.setFrame(Rectangle2D(1.0, 2.0, 3.0, 4.0))
        assertThat(invalidated, `is`(true))

    }
}