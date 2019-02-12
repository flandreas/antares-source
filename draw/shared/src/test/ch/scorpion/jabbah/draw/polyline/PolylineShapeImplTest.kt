package ch.scorpion.jabbah.draw.polyline

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawTestRule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for [PolylineShape].
 */
class PolylineShapeImplTest {

	@BeforeTest
	fun beforeTest() {
		DrawTestRule.configure()
	}

    @Test
    fun shouldCalculateLength() {
        assertEquals(0.0, PolylineShapeImpl().addPoint(0, 0).getLength())
        assertEquals(100.0, PolylineShapeImpl().addPoint(0, 0).addPoint(100, 0).getLength())
        assertEquals(300.0, PolylineShapeImpl().addPoint(0, 0).addPoint(100, 0).addPoint(100, 200).getLength())
    }

    @Test
    fun shouldFindSegment() {
        val polyline = PolylineShapeImpl().addPoint(0, 0).addPoint(100, 0).addPoint(100, 100)
        assertEquals(0, polyline.findSegment(50.0, 0.0, 5))
        assertEquals(1, polyline.findSegment(100.0, 50.0, 5))
        assertNull(polyline.findSegment(0.0, 100.0, 5))
    }

    @Test
    fun shouldMovePointsWhenSettingLocation() {
        val polyline = PolylineShapeImpl().addPoint(0, 0).addPoint(100, 0).addPoint(100, 100)
        polyline.setLocation(50.0, 50.0)
        assertEquals(Point2D(50, 50), polyline.getPointAt(0))
        assertEquals(Point2D(150, 50), polyline.getPointAt(1))
        assertEquals(Point2D(150, 150), polyline.getPointAt(2))
    }
}