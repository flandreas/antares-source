package ch.scorpion.jabbah.edit.model.polyline

import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.base.geom.Point2D
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

/**
 * Unit tests for [OrthoPolyline].
 */
class OrthoPolylineTest {

    companion object {
        @ClassRule @JvmField
        val editTestRule = EditTestRule()
    }

    @Test
    fun shouldBuild() {
        val polyLine = OrthoPolyline()

        polyLine.add(Point2D(0, 0))
        polyLine.add(Point2D(10, 0))
        polyLine.add(Point2D(20, 0))
        polyLine.add(Point2D(20, 10))

        assertThat(polyLine.size, `is`(3))

        val bbox = polyLine.boundingBox
        assertThat(bbox.x, `is`(0.0))
        assertThat(bbox.y, `is`(0.0))
        assertThat(bbox.width, `is`(20.0))
        assertThat(bbox.height, `is`(10.0))
    }

    @Test
    fun shouldBuildWithPoints() {
        val polyline = OrthoPolyline(listOf(Point2D(0, 0), Point2D(10, 0)))
        assertThat(polyline.size, `is`(2))
    }

    @Test
    fun shouldCompactCollinearPoint() {
        val polyline = OrthoPolyline()
        polyline
            .add(Point2D(0, 0))
            .add(Point2D(100, 0))
            .add(Point2D(200, 0))

        polyline.compact()

        assertThat(polyline.size, `is`(2))
    }

    @Test
    fun shouldCompactCollinearPoint2() {
        val polyline = OrthoPolyline()
        polyline
            .add(Point2D(100, 0))
            .add(Point2D(200, 0))
            .add(Point2D(0, 0))

        polyline.compact()

        assertThat(polyline.size, `is`(2))
    }

    @Test
    fun shouldCompactNonOrthogonalCollinearPoint() {
        val polyline = OrthoPolyline()
        polyline
            .add(Point2D(0, 0))
            .add(Point2D(100, 100))
            .add(Point2D(200, 200))

        polyline.compact()

        assertThat(polyline.size, `is`(2))
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

        assertThat(polyline.size, `is`(4))
    }

    @Test
    fun shouldNotCompactSmallOrthogonalSegments() {
        val polyline = OrthoPolyline()
        polyline
            .add(Point2D(0, 0))
            .add(Point2D(2, 0))
            .add(Point2D(2, 2))

        polyline.compact()

        assertThat(polyline.size, `is`(3))
    }

    @Test
    fun shouldCompactStartPoint() {
        val polyline = OrthoPolyline()
        polyline.add(Point2D(0, 0)).add(Point2D(0, 0)).add(Point2D(200, 0))

        polyline.compact()

        assertThat(polyline.size, `is`(2))
    }

    @Test
    fun shouldCompactEndPoint() {
        val polyline = OrthoPolyline()
        polyline.add(Point2D(0, 0)).add(Point2D(200, 0)).add(Point2D(200, 0))

        polyline.compact()

        assertThat(polyline.size, `is`(2))
    }
}