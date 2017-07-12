package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.geom.AbstractRectangularShape.Companion.OUT_BOTTOM
import ch.scorpion.jabbah.base.geom.AbstractRectangularShape.Companion.OUT_LEFT
import ch.scorpion.jabbah.base.geom.AbstractRectangularShape.Companion.OUT_RIGHT
import ch.scorpion.jabbah.base.geom.AbstractRectangularShape.Companion.OUT_TOP
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [Rectangle2D].
 */
class Rectangle2DTest {

    @Before
    fun init() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldConstructWithPoint() {
        val rect = Rectangle2D(Point2D(10.0, 20.0), 30.0, 40.0)
        assertEquals(10.0, rect.x, 0.1)
        assertEquals(20.0, rect.y, 0.1)
    }

    @Test
    fun shouldCopy() {
        val rect = Rectangle2D(10.0, 20.0, 30.0, 40.0)
        val copy = rect.copy()
        copy.setFrame(100.0, 100.0, 200.0, 100.0)
        assertEquals(Rectangle2D(10.0, 20.0, 30.0, 40.0), rect)
        assertEquals(Rectangle2D(100.0, 100.0, 200.0, 100.0), copy)
    }

    @Test
    fun shouldReturnMinMax() {
        assertEquals(10.0, Rectangle2D(10.0, 20.0, 30.0, 40.0).minX, 0.1)
        assertEquals(20.0, Rectangle2D(10.0, 20.0, 30.0, 40.0).minY, 0.1)
        assertEquals(40.0, Rectangle2D(10.0, 20.0, 30.0, 40.0).maxX, 0.1)
        assertEquals(60.0, Rectangle2D(10.0, 20.0, 30.0, 40.0).maxY, 0.1)
    }

    @Test
    fun shouldSetFrame() {
        val rect = Rectangle2D(0.0, 0.0, 200.0, 100.0)
        rect.setFrame(10.0, 20.0, 1000.0, 2000.0)
        assertEquals(10.0, rect.x, 0.1)
        assertEquals(20.0, rect.y, 0.1)
        assertEquals(1000.0, rect.width, 0.1)
        assertEquals(2000.0, rect.height, 0.1)
    }

    @Test
    fun shouldSetFrameFromRect() {
        val rect = Rectangle2D(0.0, 0.0, 200.0, 100.0)
        rect.setFrame(Rectangle2D(10.0, 20.0, 1000.0, 2000.0))
        assertEquals(10.0, rect.x, 0.1)
        assertEquals(20.0, rect.y, 0.1)
        assertEquals(1000.0, rect.width, 0.1)
        assertEquals(2000.0, rect.height, 0.1)
    }

    @Test
    fun shouldContainLocation() {
        var rect = Rectangle2D(0.0, 0.0, 100.0, 100.0)
        assertTrue(rect.contains(0.0, 0.0));
        assertTrue(rect.contains(100.0, 100.0));
        assertTrue(rect.contains(50.0, 50.0));
    }

    @Test
    fun shouldNotContainLocation() {
        var rect = Rectangle2D(0.0, 0.0, 100.0, 100.0)
        assertFalse(rect.contains(-1.0, -1.0));
        assertFalse(rect.contains(101.0, 100.0));
        assertFalse(rect.contains(200.0, 200.0));
    }

    @Test
    fun shouldBeEmpty() {
        assertTrue(Rectangle2D(0.0, 0.0, 0.0, 100.0).isEmpty);
        assertTrue(Rectangle2D(0.0, 0.0, 100.0, 0.0).isEmpty);
        assertFalse(Rectangle2D(0.0, 0.0, 100.0, 100.0).isEmpty);
    }

    @Test
    fun shouldContainRectangle() {
        var rect = Rectangle2D(0.0, 0.0, 100.0, 100.0)
        assertTrue(rect.contains(0.0, 0.0, 10.0, 10.0));
        assertTrue(rect.contains(0.0, 0.0, 100.0, 100.0));
        assertTrue(rect.contains(10.0, 10.0, 10.0, 10.0));
        assertFalse(rect.contains(-1.0, 1.0, 100.0, 100.0));
        assertFalse(rect.contains(0.0, 0.0, 200.0, 200.0));
        assertFalse(rect.contains(200.0, 200.0, 100.0, 100.0));
    }

    @Test
    fun shouldAddLocation() {
        assertTrue(Rectangle2D(0.0, 0.0, 100.0, 100.0).add(200.0, 200.0).contains(200.0, 200.0))
    }

    @Test
    fun shouldAddRectangle() {
        var rect = Rectangle2D(0.0, 0.0, 100.0, 100.0).add(Rectangle2D(50.0, 50.0, 100.0, 100.0))
        assertEquals(Rectangle2D(0.0, 0.0, 150.0, 150.0), rect)
    }

    @Test
    fun shouldOutcode() {
        assertThat(Rectangle2D(100, 100, 100, 100).outcode(150.0, 0.0), `is`(OUT_TOP))
        assertThat(Rectangle2D(100, 100, 100, 100).outcode(300.0, 0.0), `is`(OUT_TOP or OUT_RIGHT))
        assertThat(Rectangle2D(100, 100, 100, 100).outcode(300.0, 150.0), `is`(OUT_RIGHT))
        assertThat(Rectangle2D(100, 100, 100, 100).outcode(300.0, 300.0), `is`(OUT_BOTTOM or OUT_RIGHT))
        assertThat(Rectangle2D(100, 100, 100, 100).outcode(150.0, 300.0), `is`(OUT_BOTTOM))
        assertThat(Rectangle2D(100, 100, 100, 100).outcode(0.0, 300.0), `is`(OUT_BOTTOM or OUT_LEFT))
        assertThat(Rectangle2D(100, 100, 100, 100).outcode(0.0, 150.0), `is`(OUT_LEFT))
        assertThat(Rectangle2D(100, 100, 100, 100).outcode(0.0, 0.0), `is`(OUT_TOP or OUT_LEFT))
    }

    @Test
    fun shouldIntersectLine() {
        val rect = Rectangle2D(100, 100, 100, 100)
        assertThat(rect.intersectsLine(0.0, 150.0, 150.0, 150.0), `is`(true))
        assertThat(rect.intersectsLine(150.0, 0.0, 150.0, 150.0), `is`(true))
        assertThat(rect.intersectsLine(300.0, 150.0, 150.0, 150.0), `is`(true))
        assertThat(rect.intersectsLine(150.0, 300.0, 150.0, 150.0), `is`(true))
    }

    @Test
    fun shouldNotIntersectLine() {
        val rect = Rectangle2D(100, 100, 100, 100)
        assertThat(rect.intersectsLine(0.0, 0.0, 300.0, 0.0), `is`(false))
        assertThat(rect.intersectsLine(150.0, 0.0, 300.0, 0.0), `is`(false))
    }

    @Test
    fun shouldAddFirstPoint() {
        val rect = Rectangle2D()
        rect.add(100, 100)
        assertThat(rect.boundingBox as Rectangle2D, `is`(Rectangle2D(100, 100, 0, 0)))
    }
}