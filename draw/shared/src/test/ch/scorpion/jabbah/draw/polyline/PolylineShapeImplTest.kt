package ch.scorpion.jabbah.draw.polyline

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawTestRule
import ch.scorpion.jabbah.draw.drawable.RotationDirection
import kotlin.math.sqrt
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

	@Test
	fun shouldRotateCounterClockwise() {
		val polyline = PolylineShapeImpl(listOf(
			Point2D(0, 0),
			Point2D(100, 0),
			Point2D(100, 100)))

		polyline.rotate(RotationDirection.CounterClockwise)

		assertEquals(Point2D(0, 0), polyline.getPointAt(0))
		assertEquals(Point2D(0, -100), polyline.getPointAt(1))
		assertEquals(Point2D(100, -100), polyline.getPointAt(2))
	}

	@Test
	fun shouldRotateClockwise() {
		val polyline = PolylineShapeImpl(listOf(
			Point2D(0, 0),
			Point2D(100, 0),
			Point2D(100, 100)))

		polyline.rotate(RotationDirection.Clockwise)

		assertEquals(Point2D(0, 0), polyline.getPointAt(0))
		assertEquals(Point2D(0, 100), polyline.getPointAt(1))
		assertEquals(Point2D(-100, 100), polyline.getPointAt(2))
	}

	@Test
	fun shouldRotateAroundPivot() {
		val polyline = PolylineShapeImpl(listOf(
			Point2D(0, 0),
			Point2D(100, 0),
			Point2D(100, 100)))

		polyline.rotate(RotationDirection.CounterClockwise, Point2D(200, 0))

		assertEquals(Point2D(200, 200), polyline.getPointAt(0))
		assertEquals(Point2D(200, 100), polyline.getPointAt(1))
		assertEquals(Point2D(300, 100), polyline.getPointAt(2))
	}

	@Test
	fun shouldCalculateOrthogonalLength() {
		val polyline = PolylineShapeImpl(listOf(
			Point2D(0, 0),
			Point2D(100, 0),
			Point2D(100, 100),
			Point2D(200, 100)))

		assertEquals(300.0, polyline.length)
	}

	@Test
	fun shouldCalculateNonOrthogonalLength() {
		val polyline = PolylineShapeImpl(listOf(
			Point2D(0, 0),
			Point2D(20, 0),
			Point2D(180, 100),
			Point2D(200, 100)))

		assertEquals(20 + sqrt(100.0 * 100.0 + 160.0 * 160.0) + 20, polyline.length)
	}

	@Test
	fun shouldOverlapHorizontally() {
		assertTrue(
			PolylineShapeImpl(Point2D(0, 0), Point2D(100, 0)).overlapsOrthogonallyWith(
			0, listOf(Point2D(50, 0), Point2D(150, 0))))
	}

	@Test
	fun shouldNotOverlapHorizontallyWithDifferentY() {
		assertFalse(
			PolylineShapeImpl(Point2D(0, 50), Point2D(100, 50)).overlapsOrthogonallyWith(
				0, listOf(Point2D(50, 0), Point2D(150, 0))))
	}

	@Test
	fun shouldNotOverlapHorizontally() {
		assertFalse(
			PolylineShapeImpl(Point2D(0, 0), Point2D(100, 0)).overlapsOrthogonallyWith(
				0, listOf(Point2D(200, 0), Point2D(300, 0))))
	}

	@Test
	fun shouldOverlapVertically() {
		assertTrue(
			PolylineShapeImpl(Point2D(0, 0), Point2D(0, 100)).overlapsOrthogonallyWith(
				0, listOf(Point2D(0, 50), Point2D(0, 150))))
	}

	@Test
	fun shouldNotOverlapVerticallyWithDifferentX() {
		assertFalse(
			PolylineShapeImpl(Point2D(50, 0), Point2D(50, 100)).overlapsOrthogonallyWith(
				0, listOf(Point2D(0, 50), Point2D(0, 150))))
	}

	@Test
	fun shouldNotOverlapVertically() {
		assertFalse(
			PolylineShapeImpl(Point2D(0, 0), Point2D(0, 100)).overlapsOrthogonallyWith(
				0, listOf(Point2D(0, 150), Point2D(0, 250))))
	}

	@Test
	fun shouldCalculateIntersectionCount() {
		val polyline1 = PolylineShapeImpl(Point2D(0, 300), Point2D(100, 300), Point2D(100, 0), Point2D(200, 0))
		val polyline2 = listOf(Point2D(0, 400), Point2D(50, 400), Point2D(50, 100), Point2D(300, 100))
		val interference = polyline1.calculateInterference(polyline2)
		assertEquals(2, interference.intersectionCount)
		assertEquals(0, interference.overlappingCount)
	}

	@Test
	fun shouldCalculateOverlappingCount() {
		val polyline1 = PolylineShapeImpl(Point2D(0, 300), Point2D(100, 300), Point2D(100, 0), Point2D(200, 0))
		val polyline2 = listOf(Point2D(0, 400), Point2D(100, 400), Point2D(100, 100), Point2D(300, 100))
		val interference = polyline1.calculateInterference(polyline2)
		// Intersection at endpoints counts as well
		assertEquals(2, interference.intersectionCount)
		assertEquals(1, interference.overlappingCount)
	}
}