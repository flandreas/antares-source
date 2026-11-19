package io.antarescircuit.jabbah.edit.model.polyline

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.edit.EditTestRule
import kotlin.random.Random
import kotlin.test.*

/**
 * Unit tests for [CompactablePolyline].
 */
class CompactablePolylineTest {

	@BeforeTest
	fun setup() {
		EditTestRule.configure()
	}
	
    @Test
    fun shouldBuild() {
        val polyLine = CompactablePolyline()

        polyLine.add(Point2D(0, 0))
        polyLine.add(Point2D(10, 0))
        polyLine.add(Point2D(20, 0))
        polyLine.add(Point2D(20, 10))

        assertEquals(polyLine.size, 3)

        val bbox = polyLine.boundingBox
        assertEquals(0.0, bbox.x)
        assertEquals(0.0, bbox.y)
        assertEquals(20.0, bbox.width)
        assertEquals(10.0, bbox.height)
    }

    @Test
    fun shouldBuildWithPoints() {
        val polyline = CompactablePolyline(listOf(Point2D(0, 0), Point2D(10, 0)))
        assertEquals(2, polyline.size)
    }

    @Test
    fun shouldNotCompactDegeneratedLine() {
        val polyline = CompactablePolyline()

        polyline.add(Point2D(100, 0))
        polyline.add(Point2D(100, 0))

        polyline.compact()

        assertEquals(2, polyline.size)
    }

    @Test
    fun shouldCompactCollinearPoint() {
        val polyline = CompactablePolyline()
        polyline
            .add(Point2D(0, 0))
            .add(Point2D(100, 0))
            .add(Point2D(200, 0))

        polyline.compact()

        assertEquals(2, polyline.size)
    }

    @Test
    fun shouldCompactCollinearPoint2() {
        val polyline = CompactablePolyline()
        polyline
            .add(Point2D(100, 0))
            .add(Point2D(200, 0))
            .add(Point2D(0, 0))

        polyline.compact()

        assertEquals(2, polyline.size)
    }

    @Test
    fun shouldCompactNonOrthogonalCollinearPoint() {
        val polyline = CompactablePolyline()
        polyline
            .add(Point2D(0, 0))
            .add(Point2D(100, 100))
            .add(Point2D(200, 200))

        polyline.compact()

        assertEquals(2, polyline.size)
    }

    @Test
    fun shouldCompactNonOrthogonalCollinearPoint2() {
        val polyline = CompactablePolyline()
        polyline
            .add(Point2D(0, 0))
            .add(Point2D(100, 0))
            .add(Point2D(150, 50))
            .add(Point2D(200, 100))
            .add(Point2D(300, 100))

        polyline.compact()

        assertEquals(4, polyline.size)
    }

    @Test
    fun shouldNotCompactSmallOrthogonalSegments() {
        val polyline = CompactablePolyline()
        polyline
            .add(Point2D(0, 0))
            .add(Point2D(2, 0))
            .add(Point2D(2, 2))

        polyline.compact()

        assertEquals(3, polyline.size)
    }

    @Test
    fun shouldCompactStartPoint() {
        val polyline = CompactablePolyline()
        polyline.add(Point2D(0, 0)).add(Point2D(0, 0)).add(Point2D(200, 0))

        polyline.compact()

        assertEquals(2, polyline.size)
    }

    @Test
    fun shouldCompactEndPoint() {
        val polyline = CompactablePolyline()
        polyline.add(Point2D(0, 0)).add(Point2D(200, 0)).add(Point2D(200, 0))

        polyline.compact()

        assertEquals(2, polyline.size)
    }

	@Test
	fun shouldBeOrthogonal() {
		val polyline = CompactablePolyline()
		polyline
			.add(Point2D(0, 0))
			.add(Point2D(100, 0))
			.add(Point2D(100, 100))

		assertTrue(polyline.isOrthogonal)
	}

	@Test
	fun shouldNotBeOrthogonal() {
		val polyline = CompactablePolyline()
		polyline
			.add(Point2D(0, 0))
			.add(Point2D(100, 100))

		assertFalse(polyline.isOrthogonal)
	}

	@Test
	fun shouldNotBeOrthogonalWhenEmpty() {
		val polyline = CompactablePolyline()

		assertFalse(polyline.isOrthogonal)
	}

	@Test
	fun shouldShrinkBoundsWhenCompactingAReversal() {
		val polyline = CompactablePolyline(listOf(Point2D(0, 0), Point2D(100, 0)))
		assertEquals(100.0, polyline.boundingBox.width)
		polyline.add(Point2D(20, 0))
		assertEquals(listOf(Point2D(0, 0), Point2D(20, 0)), polyline.points)
		assertEquals(Rectangle2D(0, 0, 20, 1), polyline.boundingBox)
		polyline.add(Point2D(-10, 0))
		assertEquals(Rectangle2D(-10, 0, 11, 1), polyline.boundingBox)
	}

	@Test
	fun shouldPreserveCornersAndBoundsDuringRepeatedAppends() {
		val polyline = CompactablePolyline()
		for (x in 0..100) polyline.add(Point2D(x, 0))
		for (y in 0..100) polyline.add(Point2D(100, y))
		for (x in 100 downTo -20) polyline.add(Point2D(x, 100))
		assertEquals(listOf(Point2D(0, 0), Point2D(100, 0), Point2D(100, 100), Point2D(-20, 100)), polyline.points)
		assertEquals(Rectangle2D(-20, 0, 120, 100), polyline.boundingBox)
	}

	@Test
	fun shouldMatchFullCompactionAfterMixedAppends() {
		val random = Random(583)
		val incremental = CompactablePolyline()
		val fullScan = CompactablePolyline()
		var point = Point2D.ZERO
		repeat(1000) { step ->
			// Include duplicates, reversals and angles close to the compaction tolerance.
			point = when (step % 5) {
				0 -> point
				1 -> point.add(10.0, random.nextDouble(-0.9, 0.9))
				2 -> point.add(-5.0, random.nextDouble(-0.4, 0.4))
				else -> point.add(random.nextInt(-10, 11).toDouble(), random.nextInt(-10, 11).toDouble())
			}
			if (step % 17 == 0) {
				incremental.add(listOf(point, point.add(1.0, 1.0)))
				fullScan.add(listOf(point, point.add(1.0, 1.0)))
			} else {
				incremental.add(point)
				fullScan.add(listOf(point))
			}
			assertEquals(fullScan.points, incremental.points, "Points at step $step")
			assertEquals(fullScan.boundingBox, incremental.boundingBox, "Bounds at step $step")
		}
	}
}
