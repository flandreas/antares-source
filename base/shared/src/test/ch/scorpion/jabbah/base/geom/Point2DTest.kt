package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [Point2DTest].
 */
class Point2DTest {

    @Before
    fun init() {
        BaseModuleJvm.require()
    }

    @Test
    fun shouldAccessCoordinates() {
        assertEquals(10.0, Point2D(10.0, 20.0).x, 0.1)
        assertEquals(20.0, Point2D(10.0, 20.0).y, 0.1)
    }

    @Test
    fun shouldConstructDefault() {
        assertEquals(Point2D(0.0, 0.0), Point2D.ZERO)
    }

    @Test
    fun shouldCalculateDistanceSq() {
        assertEquals(25.0, Point2D(0.0, 0.0).distanceSq(3.0, 4.0), 0.1)
    }

    @Test
    fun shouldCalculateDistance() {
        assertEquals(5.0, Point2D(0.0, 0.0).distance(3.0, 4.0), 0.1)
    }

    @Test
    fun shouldConvertToString() {
        assertEquals("Point2D(10.0,10.0)", Point2D(10.0, 10.0).toString())
    }

    @Test
    fun shouldMirrorHorizontally() {
        assertEquals(Point2D(10, 20).mirrorHorizontally(0.0), Point2D(-10, 20))
    }

    fun shouldMirrorVertically() {
        assertEquals(Point2D(10, 20).mirrorVertically(0.0), Point2D(10, -20))
    }
}