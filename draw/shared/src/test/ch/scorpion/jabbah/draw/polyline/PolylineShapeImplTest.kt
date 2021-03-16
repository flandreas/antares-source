package ch.scorpion.jabbah.draw.polyline

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawTestRule
import kotlin.test.*

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
        assertEquals(0.0, PolylineShapeImpl().addPoint(0, 0).length)
        assertEquals(100.0, PolylineShapeImpl().addPoint(0, 0).addPoint(100, 0).length)
        assertEquals(300.0, PolylineShapeImpl().addPoint(0, 0).addPoint(100, 0).addPoint(100, 200).length)
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

	@Test
	fun shouldReverse() {
		val polyline = PolylineShapeImpl(listOf(Point2D(0, 0), Point2D(100, 0), Point2D(100, 100)))

		polyline.reverse()

		assertEquals(Point2D(100, 100), polyline.getPointAt(0))
		assertEquals(Point2D(100, 0), polyline.getPointAt(1))
		assertEquals(Point2D(0, 0), polyline.getPointAt(2))
	}

	@Test
	fun shouldRoundWhenCheckingHorizontal() {
		val polyline = PolylineShapeImpl(listOf(Point2D(0.0, 0.0), Point2D(10.0, 0.0000001)))

		assertTrue(polyline.isSegmentHorizontal(0))
	}

	@Test
	fun shouldRoundWhenCheckingVertical() {
		val polyline = PolylineShapeImpl(listOf(Point2D(0.0, 0.0), Point2D(0.0000001, 10.0)))

		assertTrue(polyline.isSegmentVertical(0))
	}

	@Test
	fun shouldCompactHorizontally() {
		val polyline = PolylineShapeImpl(listOf(Point2D(0, 0), Point2D(100, 0), Point2D(200, 0)))

		polyline.compact()

		assertEquals(2, polyline.pointsCount)
	}

	@Test
	fun shouldCompactHorizontallyWithRounding() {
		val polyline = PolylineShapeImpl(listOf(Point2D(0, 0), Point2D(100.0, 0.00000001), Point2D(200, 0)))

		polyline.compact()

		assertEquals(2, polyline.pointsCount)
	}


	@Test
	fun shouldCompactVertically() {
		val polyline = PolylineShapeImpl(listOf(Point2D(0, 0), Point2D(0, 100), Point2D(0, 200)))

		polyline.compact()

		assertEquals(2, polyline.pointsCount)
	}

	@Test
	fun shouldCompactVerticallyWithRounding() {
		val polyline = PolylineShapeImpl(listOf(Point2D(0, 0), Point2D(0.00000001, 100.0), Point2D(0, 200)))

		polyline.compact()

		assertEquals(2, polyline.pointsCount)
	}
}