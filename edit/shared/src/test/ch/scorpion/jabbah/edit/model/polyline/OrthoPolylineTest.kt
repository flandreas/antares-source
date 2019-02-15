package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.EditTestRule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for [OrthoPolyline].
 */
class OrthoPolylineTest {

	@BeforeTest
	fun setup() {
		EditTestRule.configure()
	}
	
    @Test
    fun shouldBuild() {
        val polyLine = OrthoPolyline()

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
        val polyline = OrthoPolyline(listOf(Point2D(0, 0), Point2D(10, 0)))
        assertEquals(2, polyline.size)
    }

    @Test
    fun shouldCompactCollinearPoint() {
        val polyline = OrthoPolyline()
        polyline
            .add(Point2D(0, 0))
            .add(Point2D(100, 0))
            .add(Point2D(200, 0))

        polyline.compact()

        assertEquals(2, polyline.size)
    }

    @Test
    fun shouldCompactCollinearPoint2() {
        val polyline = OrthoPolyline()
        polyline
            .add(Point2D(100, 0))
            .add(Point2D(200, 0))
            .add(Point2D(0, 0))

        polyline.compact()

        assertEquals(2, polyline.size)
    }

    @Test
    fun shouldCompactNonOrthogonalCollinearPoint() {
        val polyline = OrthoPolyline()
        polyline
            .add(Point2D(0, 0))
            .add(Point2D(100, 100))
            .add(Point2D(200, 200))

        polyline.compact()

        assertEquals(2, polyline.size)
    }

    @Test
    fun shouldCompactNonOrthogonalCollinearPoint2() {
        val polyline = OrthoPolyline()
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
        val polyline = OrthoPolyline()
        polyline
            .add(Point2D(0, 0))
            .add(Point2D(2, 0))
            .add(Point2D(2, 2))

        polyline.compact()

        assertEquals(3, polyline.size)
    }

    @Test
    fun shouldCompactStartPoint() {
        val polyline = OrthoPolyline()
        polyline.add(Point2D(0, 0)).add(Point2D(0, 0)).add(Point2D(200, 0))

        polyline.compact()

        assertEquals(2, polyline.size)
    }

    @Test
    fun shouldCompactEndPoint() {
        val polyline = OrthoPolyline()
        polyline.add(Point2D(0, 0)).add(Point2D(200, 0)).add(Point2D(200, 0))

        polyline.compact()

        assertEquals(2, polyline.size)
    }
}